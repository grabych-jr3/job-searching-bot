package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.exception.ResumeProcessingException;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@Slf4j
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
            throw new ResumeProcessingException("CV file not found for task " + taskId);
        }

        String pdfText = resumeService.extractTextFromPdf(cachedProfile);

        CandidateProfile candidateProfile;

        try {
            candidateProfile = chatClient
                    .prompt()
                    .user(u -> u.text(
                            """
                            You are given the next text from the cv of candidate: {cv}.
                            Your task is to transform this text into a JSON structure.
                            """
                    ).param("cv", pdfText))
                    .call()
                    .entity(CandidateProfile.class);
        } catch (NonTransientAiException e) {
            throw new ResumeProcessingException("Gemini failed to extract candidate profile from CV (safety or format issue)", e);
        } catch (TransientAiException | HttpClientErrorException.TooManyRequests | ResourceAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new ResumeProcessingException("Unexpected error communicating with Gemini during CV parsing", e);
        }

        if (candidateProfile == null){
            throw new ResumeProcessingException("Gemini returned empty candidate profile for task " + taskId);
        }

        candidateProfileRedisTemplate.opsForValue().set(analyzedCvCacheKey, candidateProfile, Duration.ofHours(1));

        return candidateProfile;
    }
}
