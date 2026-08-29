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
            log.debug("Caching raw CV bytes for taskId [{}] (size: {} bytes, TTL: 1h)", taskId, bytes.length);
            redisTemplate.opsForValue().set(key, bytes, Duration.ofHours(1));
        } catch (Exception e){
            log.error("Redis write failed for CV bytes key [{}]: {}", key, e.getMessage(), e);
        }
    }

    private String buildKey(String taskId){
        return "cv:" + taskId;
    }
}
