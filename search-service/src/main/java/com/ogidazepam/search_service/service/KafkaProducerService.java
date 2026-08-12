package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.JobOffer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, JobOffer> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, JobOffer> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String topic, JobOffer jobOffer){
        kafkaTemplate.send(
                topic,
                jobOffer
        );
    }
}
