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

    public OfferResult getFromCache(String cvHash, String offerUrl){
        String key = buildKey(cvHash, offerUrl);
        try {
            OfferResult result = offerResultRedisTemplate.opsForValue().get(key);
            if (result != null) {
                log.debug("Redis cache hit for analyzed offer: score={}", result.score());
            }
            return result;
        } catch (Exception e){
            log.error("Redis read failed for OfferResult key [{}]: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public void cacheOfferResult(String cvHash, String offerUrl, OfferResult result){
        String key = buildKey(cvHash, offerUrl);
        try {
            offerResultRedisTemplate.opsForValue().set(key, result, Duration.ofDays(7));
            log.debug("Cached OfferResult in Redis: score={}, TTL=7d", result.score());
        } catch (Exception e) {
            log.error("Redis write failed for OfferResult key [{}]: {}", key, e.getMessage(), e);
        }
    }

    private String buildKey(String cvHash, String offerUrl){
        return "analyzed_offer:" + ":" + cvHash + ":" + offerUrl;
    }
}
