package com.ogidazepam.search_service.utils;

import com.ogidazepam.search_service.model.JobOffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheService {

    private final RedisTemplate<String, JobOffer> redisTemplate;

    public RedisCacheService(RedisTemplate<String, JobOffer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public JobOffer getJobOfferFromCache(String id){
        String cacheKey = "found_offer:" + id;
        return redisTemplate.opsForValue().get(cacheKey);
    }

    public void writeJobOfferToCache(JobOffer jobOffer){
        String cacheKey = "found_offer:" + jobOffer.id();

        redisTemplate.opsForValue().set(cacheKey, jobOffer);
    }
}
