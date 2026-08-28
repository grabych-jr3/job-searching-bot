package com.ogidazepam.search_service.utils;

import com.ogidazepam.search_service.model.JobOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisCacheService {

    private final RedisTemplate<String, JobOffer> redisTemplate;

    public RedisCacheService(RedisTemplate<String, JobOffer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public JobOffer getJobOfferFromCache(String id){
        String key = buildKey(id);
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e){
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void writeJobOfferToCache(JobOffer jobOffer){
        String key = buildKey(jobOffer.id());
        try {
            redisTemplate.opsForValue().set(key, jobOffer);
        } catch (Exception e){
            log.warn("Redis wriye failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(String id){
        return "found_offer:" + id;
    }
}
