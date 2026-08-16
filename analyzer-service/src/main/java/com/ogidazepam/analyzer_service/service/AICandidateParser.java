package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AICandidateParser {

    private final ChatClient chatClient;
    private final ResumeService resumeService;
    private final RedisTemplate<String, byte[]> redisTemplate;

    public AICandidateParser(ChatClient.Builder chatClient, ResumeService resumeService, RedisTemplate<String, byte[]> redisTemplate) {
        this.chatClient = chatClient.build();
        this.resumeService = resumeService;
        this.redisTemplate = redisTemplate;
    }

    public CandidateProfile createCandidateProfile(String taskId){
        String cacheKey = "cv:" + taskId;

        byte[] cachedProfile = redisTemplate.opsForValue().get(cacheKey);
        if (cachedProfile == null){
            throw new RuntimeException("File not found");
        }

        String pdfText = resumeService.extractTextFromPdf(cachedProfile);

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
