package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "completed-offer-topic")
    public void consumeCompletedOffers(AnalyzedOfferEvent event){
        System.out.println(event.offerResult());
        if (event.type() == AnalyzedOfferEvent.EventType.ANALYSIS_FINISHED){
            System.out.println("ANALYSIS FINISHED");
        }
    }
}
