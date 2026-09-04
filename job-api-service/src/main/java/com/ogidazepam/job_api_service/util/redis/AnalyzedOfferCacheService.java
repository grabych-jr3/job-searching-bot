package com.ogidazepam.job_api_service.util.redis;

import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AnalyzedOfferCacheService {

    private final RedisTemplate<String, AnalyzedOffer> analyzedOfferRedisTemplate;

    public AnalyzedOfferCacheService(RedisTemplate<String, AnalyzedOffer> analyzedOfferRedisTemplate) {
        this.analyzedOfferRedisTemplate = analyzedOfferRedisTemplate;
    }

    public void deleteAllAnalyzedOffersFromCache(){
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match("analyzed_offer:*")
                .count(1000)
                .build();

        List<String> keysToDelete = new ArrayList<>();
        try(Cursor<String> cursor = analyzedOfferRedisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()){
                keysToDelete.add(cursor.next());

                if (keysToDelete.size() >= 500){
                    analyzedOfferRedisTemplate.unlink(keysToDelete);
                    keysToDelete.clear();
                }
            }

            if (!keysToDelete.isEmpty()){
                analyzedOfferRedisTemplate.unlink(keysToDelete);
            }

            log.info("Successfully flushed all analyzed offer caches from Redis");
        } catch (Exception e){
            log.error("Failed to clean up analyzed offer keys from Redis: {}", e.getMessage(), e);
        }
    }

    public void deleteOfferFromCache(String cvHash, String url){
        String key = buildKey(cvHash, url);
        try {
            analyzedOfferRedisTemplate.delete(key);
            log.debug("Deleted AnalyzedOffer from Redis: key=[{}]", key);
        } catch (Exception e){
            log.warn("Redis delete failed for AnalyzedOffer key [{}]: {}", key, e.getMessage());
        }

    }

    private String buildKey(String cvHash, String url){
        return "analyzed_offer:" + cvHash + ":" + url;
    }
}
