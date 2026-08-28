package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.config.KafkaConfig;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.util.redis.RedisSsePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    private final RedisSsePublisher redisSsePublisher;
    private final AnalyzedOfferService analyzedOfferService;

    public KafkaConsumerService(RedisSsePublisher redisSsePublisher, AnalyzedOfferService analyzedOfferService) {
        this.redisSsePublisher = redisSsePublisher;
        this.analyzedOfferService = analyzedOfferService;
    }

    @RetryableTopic(attempts = "4", dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = KafkaConfig.CONSUMING_TOPIC)
    public void consumeCompletedOffers(
            @Payload AnalyzedOfferEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String taskId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.info("Received message: {} from partition: {}, offset: {}", taskId, partition, offset);
        if (event.type() == AnalyzedOfferEvent.EventType.OFFER){
            analyzedOfferService.saveAnalyzedOffer(event);
        }

        redisSsePublisher.publish(event);
    }

    @DltHandler
    public void handleDltEvent(
            @Payload AnalyzedOfferEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.warn("Message was sent to DLT {} on offset: {}. Payload: {}", topic, offset, event);
    }
}
