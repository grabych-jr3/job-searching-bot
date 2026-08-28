package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.exception.ResumeProcessingException;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.redis.CVBytesCacheService;
import com.ogidazepam.analyzer_service.redis.CandidateProfileCacheService;
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

    private final CVBytesCacheService cvBytesCacheService;
    private final CandidateProfileCacheService candidateProfileCacheService;

    public AICandidateParser(ChatClient.Builder chatClient, ResumeService resumeService, CVBytesCacheService cvBytesCacheService, CandidateProfileCacheService candidateProfileCacheService) {
        this.chatClient = chatClient.build();
        this.resumeService = resumeService;
        this.cvBytesCacheService = cvBytesCacheService;
        this.candidateProfileCacheService = candidateProfileCacheService;
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

        CandidateProfile analyzedCandidateProfile = candidateProfileCacheService.getFromCache(taskId);
        if (analyzedCandidateProfile != null){
            return analyzedCandidateProfile;
        }

        byte[] cachedProfile = cvBytesCacheService.getFromCache(taskId);
        if (cachedProfile == null){
            throw new ResumeProcessingException("CV file not found for task " + taskId);
        }

        String pdfText = resumeService.extractTextFromPdf(cachedProfile);

        CandidateProfile candidateProfile = parseCandidateCv(pdfText);

        if (candidateProfile == null){
            throw new ResumeProcessingException("Gemini returned empty candidate profile for task " + taskId);
        }

        candidateProfileCacheService.cacheCandidateProfile(taskId, candidateProfile);

        return candidateProfile;
    }

    private CandidateProfile parseCandidateCv(String pdfText){
        try {
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
        } catch (NonTransientAiException e) {
            throw new ResumeProcessingException("Gemini failed to extract candidate profile from CV (safety or format issue)", e);
        } catch (TransientAiException | HttpClientErrorException.TooManyRequests | ResourceAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new ResumeProcessingException("Unexpected error communicating with Gemini during CV parsing", e);
        }
    }
}
