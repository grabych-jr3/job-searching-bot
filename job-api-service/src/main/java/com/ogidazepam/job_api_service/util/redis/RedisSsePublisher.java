package com.ogidazepam.job_api_service.util.redis;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisSsePublisher {

    public static final String SSE_TOPIC = "sse:analyzed-offers";
    private final RedisTemplate<String, AnalyzedOfferEvent> redisTemplate;

    public RedisSsePublisher(RedisTemplate<String, AnalyzedOfferEvent> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(AnalyzedOfferEvent event){
        log.debug("Publishing AnalyzedOfferEvent to Redis topic [{}] for taskId [{}], type: {}",
                SSE_TOPIC, event.taskId(), event.type());
        redisTemplate.convertAndSend(SSE_TOPIC, event);
    }
}
