package com.ogidazepam.analyzer_service.redis;

import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class CandidateProfileCacheService {

    private final RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate;

    public CandidateProfileCacheService(RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate) {
        this.candidateProfileRedisTemplate = candidateProfileRedisTemplate;
    }

    public CandidateProfile getFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            return candidateProfileRedisTemplate.opsForValue().get(key);
        } catch (Exception e){
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void cacheCandidateProfile(String taskId, CandidateProfile candidateProfile){
        String key = buildKey(taskId);
        try {
            candidateProfileRedisTemplate.opsForValue().set(key, candidateProfile, Duration.ofHours(1));
        } catch (Exception e){
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    public void deleteFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            candidateProfileRedisTemplate.delete(key);
        } catch (Exception e){
            log.warn("Redis delete failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String taskId){
        return "analyzed:cv:" + taskId;
    }
}
