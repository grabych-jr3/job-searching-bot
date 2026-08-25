package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final AnalyzedOfferService analyzedOfferService;
    private final SSENotificationService sseNotificationService;

    public KafkaConsumerService(AnalyzedOfferService analyzedOfferService, SSENotificationService sseNotificationService) {
        this.analyzedOfferService = analyzedOfferService;
        this.sseNotificationService = sseNotificationService;
    }

    @KafkaListener(topics = "completed-offer-topic")
    public void consumeCompletedOffers(AnalyzedOfferEvent event){
        String taskId = event.taskId();

        switch (event.type()) {
            case OFFER -> {
                sseNotificationService.sendOffer(taskId, event.offerResult());
                analyzedOfferService.saveAnalyzedOffer(event);
            }
            case ANALYSIS_FINISHED -> sseNotificationService.sendCompletion(taskId);
            case ANALYSIS_FAILED -> sseNotificationService.sendFailure(taskId, event.errorMessage());
        }
    }
}
