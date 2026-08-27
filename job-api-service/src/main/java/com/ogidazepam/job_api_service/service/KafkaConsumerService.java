package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.util.redis.RedisSsePublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final RedisSsePublisher redisSsePublisher;
    private final AnalyzedOfferService analyzedOfferService;

    public KafkaConsumerService(RedisSsePublisher redisSsePublisher, AnalyzedOfferService analyzedOfferService) {
        this.redisSsePublisher = redisSsePublisher;
        this.analyzedOfferService = analyzedOfferService;
    }

    @KafkaListener(topics = "completed-offer-topic")
    public void consumeCompletedOffers(AnalyzedOfferEvent event){
        if (event.type() == AnalyzedOfferEvent.EventType.OFFER){
            analyzedOfferService.saveAnalyzedOffer(event);
        }

        redisSsePublisher.publish(event);
    }
}
