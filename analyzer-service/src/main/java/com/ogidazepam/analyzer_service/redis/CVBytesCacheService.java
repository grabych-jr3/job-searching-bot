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
            byte[] bytes = cvRedisTemplate.opsForValue().get(key);
            if (bytes != null) {
                log.debug("Redis cache hit for raw CV bytes: key=[{}] ({} bytes)", key, bytes.length);
            } else {
                log.warn("Redis cache miss for raw CV bytes: key=[{}]", key);
            }
            return bytes;
        } catch (Exception e) {
            log.error("Redis read failed for CV bytes key [{}]: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public void deleteFromCache(String taskId){
        String key = buildKey(taskId);
        try {
            cvRedisTemplate.delete(key);
            log.debug("Deleted raw CV bytes from Redis: key=[{}]", key);
        } catch (Exception e){
            log.warn("Redis delete failed for CV bytes key [{}]: {}", key, e.getMessage());
        }
    }

    private String buildKey(String taskId){
        return "cv:" + taskId;
    }
}
