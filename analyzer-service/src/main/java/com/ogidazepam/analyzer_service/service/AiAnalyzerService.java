package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.config.KafkaConfig;
import com.ogidazepam.analyzer_service.exception.AiAnalysisException;
import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.analyzer_service.model.event.JobOfferEvent;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import com.ogidazepam.analyzer_service.redis.OfferResultCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

@Slf4j
@Service
public class AiAnalyzerService {

    private final ChatClient chatClient;
    private final AICandidateParser aiCandidateParser;
    private final OfferResultCacheService offerResultCacheService;
    private final KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService;

    public AiAnalyzerService(ChatClient.Builder chatClient, AICandidateParser aiCandidateParser, OfferResultCacheService offerResultCacheService, KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService) {
        this.chatClient = chatClient.build();
        this.aiCandidateParser = aiCandidateParser;
        this.offerResultCacheService = offerResultCacheService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Retryable(
            includes = {
                    TransientAiException.class,
                    HttpClientErrorException.TooManyRequests.class,
                    ResourceAccessException.class
            },
            maxRetries = 3,
            delay = 2000,
            multiplier = 2,
            maxDelay = 8000,
            jitter = 300
    )
    public void analyze(JobOfferEvent event, List<JobOffer> offers){
        log.info("Starting AI suitability analysis for {} job offers (taskId: [{}], customerId: [{}])",
                offers.size(), event.taskId(), event.customerId());

        CandidateProfile candidateProfile = aiCandidateParser.createCandidateProfile(event.taskId());
        List<OfferResult> offerResults = evaluate(candidateProfile, offers, event.taskId());

        if (offerResults != null){
            log.info("Gemini AI successfully evaluated {}/{} job offers for taskId [{}]",
                    offerResults.size(), offers.size(), event.taskId());

            offerResults.forEach(offer -> {
                log.debug("Offer evaluated: [{}] -> score: {}/100, reason: [{}]",
                        offer.jobTitle(), offer.score(), offer.reason());

                kafkaProducerService.sendToKafka(
                        KafkaConfig.MAIN_TOPIC,
                        event.taskId(),
                        AnalyzedOfferEvent.offerResult(event.taskId(), event.customerId(), event.cvHash(), offer)
                );

                offerResultCacheService.cacheOfferResult(event.customerId(), event.cvHash(), offer.url(), offer);
            });
        }
    }

    private List<OfferResult> evaluate(CandidateProfile candidateProfile, List<JobOffer> offers, String taskId){
        try {
            return chatClient.prompt()
                    .system(s -> s.text(
                            """
                            You are a technical recruiter. Your task is to determine whether this candidate {candidate}
                            is suitable for specific job offers on a scale of 0–100 and give a brief justification (15 words).
                            
                            What you need to consider:
                            1. Location: determine if the candidate is able to work in the place where the job is located.
                            For example: candidate in Kraków. They can work in Katowice for some days because the distance between cities is small,
                            but they can't work in Poznań (except when work is remote). You must to  mention it in reason;
                            2. Work time: if candidate is studying, then full-time job won't be the best option;
                            3. Compare candidate's tech skills and job offer requirements. Analyze the job description and requirements in detail.
                            """).param("candidate", candidateProfile)
                    )
                    .user(
                            u -> u.text("Job offers: {offers}").param("offers", offers)
                    )
                    .call()
                    .entity(new ParameterizedTypeReference<List<OfferResult>>(){});
        } catch (NonTransientAiException e){
            log.error("Non-transient Gemini AI error while evaluating job offers for taskId [{}]: {}", taskId, e.getMessage(), e);
            throw new AiAnalysisException("Gemini analysis failed due to model output formatting or safety violation", e);
        } catch (TransientAiException | HttpClientErrorException.TooManyRequests | ResourceAccessException e) {
            log.warn("Transient/rate-limit error from Gemini AI during job offer evaluation for taskId [{}]: {}. Will retry.", taskId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error evaluating offers with Gemini for taskId [{}]: {}", taskId, e.getMessage(), e);
            throw new AiAnalysisException("Failed to analyze batch of offers with Gemini: " + e.getMessage(), e);
        }
    }
}
