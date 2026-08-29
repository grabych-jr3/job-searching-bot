package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.config.KafkaConfig;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
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

    private final JobSearchService jobSearchService;

    public KafkaConsumerService(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @RetryableTopic(attempts = "4", dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = KafkaConfig.CONSUMING_TOPIC)
    public void consume(
            @Payload CreatedTaskEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String taskId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.info("Consumed CreatedTaskEvent for taskId: {}, customerId: {}, tech: {} (partition: {}, offset: {})",
                taskId, event.customerId(), event.analyzeRequest().technology(), partition, offset);
        jobSearchService.searchAll(event);
    }

    @DltHandler
    public void handleDltEvent(
            @Payload CreatedTaskEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.error("CreatedTaskEvent routed to DLT topic [{}] on offset [{}]. TaskId: {}, customerId: {}",
                topic, offset, event.taskId(), event.customerId());
    }
}
