package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAnalyzerService {

    private final ChatClient chatClient;
    private final AICandidateParser aiCandidateParser;

    public AiAnalyzerService(ChatClient.Builder chatClient, AICandidateParser aiCandidateParser) {
        this.chatClient = chatClient.build();
        this.aiCandidateParser = aiCandidateParser;
    }

    public void analyze(List<JobOffer> offers){
        CandidateProfile candidateProfile = aiCandidateParser.createCandidateProfile();

        List<OfferResult> offerResults = chatClient.prompt()
                .system(s -> s.text(
                        """
                        You are a technical recruiter. Your task is to determine whether this candidate {candidate}
                        is suitable for specific job offers on a scale of 0–100 and give a brief justification (15 words).
                        
                        Output format is strictly JSON:
                        [
                            "url": "", "score": 1, "reason": ""
                        ]
                        """).param("candidate", candidateProfile)
                )
                .user(
                        u -> u.text("Job offers: {offers}").param("offers", offers)
                )
                .call()
                .entity(new ParameterizedTypeReference<List<OfferResult>>(){});

        listOfferResults(offerResults);
    }

    private void listOfferResults(List<OfferResult> offerResults){
        System.out.println("-----------------------");
        for (OfferResult result : offerResults){
            System.out.println(result);
        }
        System.out.println("-----------------------");
    }
}
