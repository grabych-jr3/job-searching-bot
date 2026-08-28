package com.ogidazepam.analyzer_service.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CVBytesCacheService {

    private final RedisTemplate<String, byte[]> cvRedisTemplate;

    public CVBytesCacheService(RedisTemplate<String, byte[]> cvRedisTemplate) {
        this.cvRedisTemplate = cvRedisTemplate;
    }

    public byte[] getFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            return cvRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void deleteFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            cvRedisTemplate.delete(taskId);
        } catch (Exception e){
            log.warn("Redis delete failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String taskId){
        return "cv:" + taskId;
    }
}
