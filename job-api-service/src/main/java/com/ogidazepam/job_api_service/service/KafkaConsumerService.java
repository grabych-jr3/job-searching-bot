package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final SSENotificationService sseNotificationService;

    public KafkaConsumerService(SSENotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @KafkaListener(topics = "completed-offer-topic")
    public void consumeCompletedOffers(AnalyzedOfferEvent event){
        String taskId = event.taskId();

        if (event.type() == AnalyzedOfferEvent.EventType.OFFER){
            sseNotificationService.sendOffer(taskId, event.offerResult());
        }else {
            sseNotificationService.sendCompletion(taskId);
        }
    }
}
