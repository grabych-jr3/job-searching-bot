package com.ogidazepam.job_api_service.util.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class CvBytesCacheService {

    private final RedisTemplate<String, byte[]> redisTemplate;

    public CvBytesCacheService(RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheCvBytes(String taskId, byte[] bytes){
        String key = buildKey(taskId);
        try {
            redisTemplate.opsForValue().set(key, bytes, Duration.ofHours(1));
        } catch (Exception e){
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String taskId){
        return "cv:" + taskId;
    }
}
