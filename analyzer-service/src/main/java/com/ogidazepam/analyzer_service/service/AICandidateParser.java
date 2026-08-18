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
    private final RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate;

    public AICandidateParser(ChatClient.Builder chatClient, ResumeService resumeService, RedisTemplate<String, byte[]> redisTemplate, RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate) {
        this.chatClient = chatClient.build();
        this.resumeService = resumeService;
        this.redisTemplate = redisTemplate;
        this.candidateProfileRedisTemplate = candidateProfileRedisTemplate;
    }

    public CandidateProfile createCandidateProfile(String taskId){
        String analyzedCvCacheKey = "analyzed:cv:" + taskId;

        CandidateProfile analyzedCandidateProfile = candidateProfileRedisTemplate
                .opsForValue()
                .get(analyzedCvCacheKey);
        if (analyzedCandidateProfile != null){
            return analyzedCandidateProfile;
        }

        String cacheKey = "cv:" + taskId;

        byte[] cachedProfile = redisTemplate.opsForValue().get(cacheKey);
        if (cachedProfile == null){
            throw new RuntimeException("File not found");
        }

        String pdfText = resumeService.extractTextFromPdf(cachedProfile);

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
            candidateProfileRedisTemplate.opsForValue().set(analyzedCvCacheKey, candidateProfile, Duration.ofHours(1));
        }

        return candidateProfile;
    }
}
