package com.ogidazepam.job_api_service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, T> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(
            String topic,
            T event
    ){
        kafkaTemplate.send(topic, event);
    }
}
