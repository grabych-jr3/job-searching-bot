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
            CandidateProfile profile = candidateProfileRedisTemplate.opsForValue().get(key);
            if (profile != null) {
                log.debug("Redis cache hit for CandidateProfile: key=[{}]", key);
            }
            return profile;
        } catch (Exception e){
            log.error("Redis read failed for CandidateProfile key [{}]: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public void cacheCandidateProfile(String taskId, CandidateProfile candidateProfile){
        String key = buildKey(taskId);
        try {
            candidateProfileRedisTemplate.opsForValue().set(key, candidateProfile, Duration.ofHours(1));
            log.debug("Cached CandidateProfile in Redis: key=[{}] (TTL: 1h)", key);
        } catch (Exception e){
            log.error("Redis write failed for CandidateProfile key [{}]: {}", key, e.getMessage(), e);
        }
    }

    public void deleteFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            candidateProfileRedisTemplate.delete(key);
            log.debug("Deleted CandidateProfile from Redis: key=[{}]", key);
        } catch (Exception e){
            log.warn("Redis delete failed for CandidateProfile key [{}]: {}", key, e.getMessage());
        }
    }

    private String buildKey(String taskId){
        return "analyzed:cv:" + taskId;
    }
}
