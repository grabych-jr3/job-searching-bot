package com.ogidazepam.search_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService<T> {

    private final KafkaTemplate<String, T> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, T> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendToKafka(String topic, String key, T offerEvent){
        kafkaTemplate.send(topic, key, offerEvent)
                .whenComplete((result, ex) -> {
                    if (ex == null){
                        log.info("Message sent successfully for task: {} to partition [{}]: with offset: [{}]",
                                key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send message for task: {}", key, ex);
                    }
                });
    }
}
