package com.ogidazepam.analyzer_service.service;

import com.google.genai.errors.ApiException;
import com.google.genai.errors.ServerException;
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
                    ApiException.class,
                    ResourceAccessException.class
            },
            maxRetries = 3,
            delay = 2000,
            multiplier = 2,
            maxDelay = 8000,
            jitter = 300
    )
    public void analyze(JobOfferEvent event, List<JobOffer> offers){
        log.info("Starting AI suitability analysis for {} job offers (taskId: [{}])",
                offers.size(), event.taskId());

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
                        AnalyzedOfferEvent.offerResult(event.taskId(), event.cvHash(), offer)
                );

                offerResultCacheService.cacheOfferResult(event.cvHash(), offer.url(), offer);
            });
        }
    }

    private List<OfferResult> evaluate(CandidateProfile candidateProfile, List<JobOffer> offers, String taskId){
        try {
            return chatClient.prompt()
                    .system(s -> s.text(
                            """
                                            You are a strict Technical Recruiter and Resume Matcher.
                                            Your goal is to evaluate candidate suitability for a list of job offers strictly based on provided facts.
                            
                                            ### EVALUATION RULES:
                                            1. CRITICAL CONSTRAINTS (Fail Fast):
                                               - Location/Relocation: Candidate can work locally, in commuting range (<= 1 hour), or remotely. If the job is strictly hybrid/onsite in an unreachable city without remote option, the max score is 20%.
                            
                                            2. TECHNICAL MATCH (Primary Weight: 70% of score):
                                               - Compare candidate's explicit skills against mandatory requirements (Must-Have) and optional (Nice-to-Have).
                                               - NO ASSUMPTIONS: If a tool/framework is not mentioned in the candidate profile, consider proficiency as ZERO.
                                               - Experience level: Match candidate's years of experience or seniority with job requirements.
                            
                                            3. SCORING SCALE (0–100%):
                                               - 80-100%: Perfect match on mandatory skills and location.
                                               - 70-79%: Strong match, missing 1-2 nice-to-have skills or minor experience gap.
                                               - 50-69%: Partial match, missing key mandatory skills or potential schedule conflict.
                                               - 0-49%: Mismatch in location, or missing critical mandatory stack.

                                            Candidate's CV: {candidate}
                            """).param("candidate", candidateProfile)
                    )
                    .user(
                            u -> u.text("Evaluate the following job offers for the candidate: {offers}").param("offers", offers)
                    )
                    .call()
                    .entity(new ParameterizedTypeReference<List<OfferResult>>(){});
        } catch (NonTransientAiException e){
            log.error("Non-transient Gemini AI error while evaluating job offers for taskId [{}]: {}", taskId, e.getMessage(), e);
            throw new AiAnalysisException("Gemini analysis failed due to model output formatting or safety violation", e);
        } catch (ApiException | TransientAiException | ResourceAccessException e) {
            log.warn("Transient/rate-limit error from Gemini AI during job offer evaluation for taskId [{}]: {}. Will retry.", taskId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error evaluating offers with Gemini for taskId [{}]: {}", taskId, e.getMessage(), e);
            throw new AiAnalysisException("Failed to analyze batch of offers with Gemini: " + e.getMessage(), e);
        }
    }
}
