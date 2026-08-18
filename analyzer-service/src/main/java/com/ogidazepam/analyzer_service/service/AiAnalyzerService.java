package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAnalyzerService {

    private final ChatClient chatClient;
    private final AICandidateParser aiCandidateParser;
    private final KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService;

    public AiAnalyzerService(ChatClient.Builder chatClient, AICandidateParser aiCandidateParser, KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService) {
        this.chatClient = chatClient.build();
        this.aiCandidateParser = aiCandidateParser;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void analyze(String taskId, List<JobOffer> offers){
        CandidateProfile candidateProfile = aiCandidateParser.createCandidateProfile(taskId);

        List<OfferResult> offerResults = chatClient.prompt()
                .system(s -> s.text(
                        """
                        You are a technical recruiter. Your task is to determine whether this candidate {candidate}
                        is suitable for specific job offers on a scale of 0–100 and give a brief justification (15 words).
                        
                        What you need to consider:
                        1. Location: determine if the candidate is able to work in the place where the job is located.
                        For example: candidate in Kraków. They can work in Katowice for some days because the distance between cities is small,
                        but they can't work in Poznań (except when work is remote). You must to  mention it in reason;
                        2. Work time: if candidate is studying, then full-time job won't be the best option;
                        """).param("candidate", candidateProfile)
                )
                .user(
                        u -> u.text("Job offers: {offers}").param("offers", offers)
                )
                .call()
                .entity(new ParameterizedTypeReference<List<OfferResult>>(){});

        if (offerResults != null){
            offerResults.forEach(offer -> kafkaProducerService.sendToKafka("completed-offer-topic", AnalyzedOfferEvent.offerResult(taskId, offer)));
        }
    }
}
