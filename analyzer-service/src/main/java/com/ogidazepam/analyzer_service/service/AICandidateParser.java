package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class AICandidateParser {

    private final ChatClient chatClient;
    private final ResumeService resumeService;

    public AICandidateParser(ChatClient.Builder chatClient, ResumeService resumeService) {
        this.chatClient = chatClient.build();
        this.resumeService = resumeService;
    }

    public CandidateProfile createCandidateProfile(){
        String pdfText = resumeService.extractTextFromPdf();

        return chatClient
                .prompt()
                .user(u -> u.text(
                        """
                        You are given the next text from the cv of candidate: {cv}.
                        Your task is to transform this text into a JSON structure.
                        """
                ).param("cv", pdfText))
                .call()
                .entity(CandidateProfile.class);
    }
}
