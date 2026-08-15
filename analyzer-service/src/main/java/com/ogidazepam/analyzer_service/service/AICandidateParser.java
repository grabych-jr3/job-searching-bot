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
    private final RedisTemplate<String, CandidateProfile> redisTemplate;

    public AICandidateParser(ChatClient.Builder chatClient, ResumeService resumeService, RedisTemplate<String, CandidateProfile> redisTemplate) {
        this.chatClient = chatClient.build();
        this.resumeService = resumeService;
        this.redisTemplate = redisTemplate;
    }

    public CandidateProfile getOrCreateCandidateProfile(String taskId){
        String cacheKey = "cv_profile:" + taskId;

        CandidateProfile cachedProfile = redisTemplate.opsForValue().get(cacheKey);
        if (cachedProfile != null){
            return cachedProfile;
        }

        String pdfText = resumeService.extractTextFromPdf();

        CandidateProfile candidateProfile = chatClient
                .prompt()
                .user(u -> u.text(
                        """
                        You are given the next text from the cv of candidate: {cv}.
                        Your task is to transform this text into a JSON structure.
                        """
                ).param("cv", pdfText))
                .call()
                .entity(CandidateProfile.class);

        if (candidateProfile != null){
            redisTemplate.opsForValue().set(cacheKey, candidateProfile, Duration.ofHours(1));
        }

        return candidateProfile;
    }
}
