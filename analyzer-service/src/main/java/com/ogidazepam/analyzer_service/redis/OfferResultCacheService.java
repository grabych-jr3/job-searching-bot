package com.ogidazepam.analyzer_service.redis;

import com.ogidazepam.analyzer_service.model.OfferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class OfferResultCacheService {

    private final RedisTemplate<String, OfferResult> offerResultRedisTemplate;

    public OfferResultCacheService(RedisTemplate<String, OfferResult> offerResultRedisTemplate) {
        this.offerResultRedisTemplate = offerResultRedisTemplate;
    }

    public OfferResult getFromCache(Long customerId, String cvHash, String offerUrl){
        String key = buildKey(customerId, cvHash, offerUrl);
        try {
            return offerResultRedisTemplate.opsForValue().get(key);
        } catch (Exception e){
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void cacheOfferResult(Long customerId, String cvHash, String offerUrl, OfferResult result){
        String key = buildKey(customerId, cvHash, offerUrl);
        try {
            offerResultRedisTemplate.opsForValue().set(key, result, Duration.ofDays(7));
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    private String buildKey(Long customerId, String cvHash, String offerUrl){
        return "analyzed_offer:" + customerId + ":" + cvHash + ":" + offerUrl;
    }
}
