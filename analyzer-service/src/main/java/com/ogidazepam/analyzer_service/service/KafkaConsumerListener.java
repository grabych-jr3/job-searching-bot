package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.config.KafkaConfig;
import com.ogidazepam.analyzer_service.exception.AiAnalysisException;
import com.ogidazepam.analyzer_service.exception.ResumeProcessingException;
import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.analyzer_service.model.event.JobOfferEvent;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import com.ogidazepam.analyzer_service.redis.CVBytesCacheService;
import com.ogidazepam.analyzer_service.redis.CandidateProfileCacheService;
import com.ogidazepam.analyzer_service.redis.OfferResultCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KafkaConsumerListener {

    private static final int BUFFER_MAX_SIZE = 10;
    private final Map<String, List<JobOffer>> buffers = new ConcurrentHashMap<>();
    private final Set<String> failedTasks = ConcurrentHashMap.newKeySet();

    private final AiAnalyzerService analyzerService;
    private final KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService;

    private final CVBytesCacheService cvBytesCacheService;
    private final OfferResultCacheService offerResultCacheService;
    private final CandidateProfileCacheService candidateProfileRedisTemplate;

    public KafkaConsumerListener(AiAnalyzerService analyzerService, KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService, CVBytesCacheService cvBytesCacheService, OfferResultCacheService offerResultCacheService, CandidateProfileCacheService candidateProfileRedisTemplate) {
        this.analyzerService = analyzerService;
        this.cvBytesCacheService = cvBytesCacheService;
        this.kafkaProducerService = kafkaProducerService;
        this.offerResultCacheService = offerResultCacheService;
        this.candidateProfileRedisTemplate = candidateProfileRedisTemplate;
    }

    @RetryableTopic(attempts = "4", dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR, topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = KafkaConfig.CONSUMING_TOPIC)
    public void consume(
            @Payload JobOfferEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String taskIdKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.info("Received event: {} from partition: {}, offset: {}", taskIdKey, partition, offset);
        String taskId = event.taskId();

        if (failedTasks.contains(taskId)){
            if (event.type() == JobOfferEvent.EventType.SEARCH_FINISHED){
                failedTasks.remove(taskId);
                cleanup(taskId);
            }
            return;
        }

        if(event.type() == JobOfferEvent.EventType.OFFER){
            List<JobOffer> taskBuffer = buffers.computeIfAbsent(taskId, k -> Collections.synchronizedList(new ArrayList<>()));

            OfferResult cachedOfferResult = offerResultCacheService.getFromCache(event.customerId(), event.cvHash(), event.offer().url());
            if (cachedOfferResult != null){
                kafkaProducerService.sendToKafka(KafkaConfig.MAIN_TOPIC, event.taskId(), AnalyzedOfferEvent.offerResult(taskId, event.customerId(), event.cvHash(), cachedOfferResult));
            } else {
                taskBuffer.add(event.offer());

                if (taskBuffer.size() >= BUFFER_MAX_SIZE){
                    flushBuffer(event);
                }
            }
        } else{
            flushBuffer(event);
            buffers.remove(taskId);
            if (!failedTasks.contains(taskId)){
                kafkaProducerService.sendToKafka(KafkaConfig.MAIN_TOPIC, event.taskId(), AnalyzedOfferEvent.finished(taskId, event.customerId()));
            }
            cleanup(taskId);
        }
    }

    private void flushBuffer(JobOfferEvent event){
        List<JobOffer> taskBuffer = buffers.get(event.taskId());

        if (taskBuffer != null && !taskBuffer.isEmpty()){
            List<JobOffer> batchToSend = new ArrayList<>(taskBuffer);
            taskBuffer.clear();

            try {
                analyzerService.analyze(event, batchToSend);
            } catch (ResumeProcessingException e){
                log.error("Fatal CV parsing error for task {}: {}", event.taskId(), e.getMessage());
                failedTasks.add(event.taskId());
                buffers.remove(event.taskId());
                kafkaProducerService.sendToKafka(
                        KafkaConfig.MAIN_TOPIC,
                        event.taskId(),
                        AnalyzedOfferEvent.failed(event.taskId(), event.customerId(), e.getMessage())
                );
            } catch (AiAnalysisException e){
                log.error("Failed to analyze a batch of {} offers for task {}: {}", batchToSend.size(), event.
                        taskId(), e.getMessage(), e);
            } catch (Exception e){
                log.error("Unexpected error in flushBuffer for task {}: {}", event.taskId(), e.getMessage(), e);
            }
        }
    }

    private void cleanup(String taskId){
        cvBytesCacheService.deleteFromCache(taskId);
        candidateProfileRedisTemplate.deleteFromCache(taskId);
    }

    @DltHandler
    public void handleDltEvent(
            @Payload JobOfferEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.warn("Message was sent to DLT {} on offset: {}. Payload: {}", topic, offset, event);
    }
}
