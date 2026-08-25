package com.ogidazepam.job_api_service.util;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSsePublisher {

    public static final String SSE_TOPIC = "sse:analyzed-offers";
    private final RedisTemplate<String, AnalyzedOfferEvent> redisTemplate;

    public RedisSsePublisher(RedisTemplate<String, AnalyzedOfferEvent> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(AnalyzedOfferEvent event){
        redisTemplate.convertAndSend(SSE_TOPIC, event);
    }
}
