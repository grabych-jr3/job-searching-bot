package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.analyzer_service.model.event.JobOfferEvent;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KafkaConsumerListener {

    private static final int BUFFER_MAX_SIZE = 10;
    private final Map<String, List<JobOffer>> buffers = new ConcurrentHashMap<>();
    private final Set<String> failedTasks = ConcurrentHashMap.newKeySet();

    private final AiAnalyzerService analyzerService;
    private final RedisTemplate<String, byte[]> redisTemplate;
    private final RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate;
    private final RedisTemplate<String, OfferResult> offerResultRedisTemplate;
    private final KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService;

    public KafkaConsumerListener(AiAnalyzerService analyzerService, RedisTemplate<String, byte[]> redisTemplate, RedisTemplate<String, CandidateProfile> candidateProfileRedisTemplate, RedisTemplate<String, OfferResult> offerResultRedisTemplate, KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService) {
        this.analyzerService = analyzerService;
        this.redisTemplate = redisTemplate;
        this.candidateProfileRedisTemplate = candidateProfileRedisTemplate;
        this.offerResultRedisTemplate = offerResultRedisTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = "found-offers-topic")
    public void consume(JobOfferEvent event){
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

            String cacheKey = "analyzed_offer:" + event.customerId() + ":" + event.offer().url();
            OfferResult cachedOfferResult = offerResultRedisTemplate.opsForValue().get(cacheKey);
            if (cachedOfferResult == null){
                taskBuffer.add(event.offer());

                if (taskBuffer.size() >= BUFFER_MAX_SIZE){
                    flushBuffer(event);
                }
            }
        } else{
            flushBuffer(event);
            buffers.remove(taskId);
            if (!failedTasks.contains(taskId)){
                kafkaProducerService.sendToKafka("completed-offer-topic", event.taskId(), AnalyzedOfferEvent.finished(taskId, event.customerId()));
            }
            cleanup(taskId);
        }
    }

    private void flushBuffer(JobOfferEvent event){
        List<JobOffer> taskBuffer = buffers.get(event.taskId());

        if (taskBuffer != null && !taskBuffer.isEmpty()){
            List<JobOffer> batchToSend = new ArrayList<>(taskBuffer);

            try {
                analyzerService.analyze(event, batchToSend);
            } catch (Exception e){
                failedTasks.add(event.taskId());
                buffers.remove(event.taskId());
                kafkaProducerService.sendToKafka(
                        "completed-offer-topic",
                        event.taskId(),
                        AnalyzedOfferEvent.failed(event.taskId(), event.customerId(), e.getMessage())
                );
            }

            taskBuffer.clear();
        }
    }

    private void cleanup(String taskId){
        redisTemplate.delete("cv:" + taskId);
        candidateProfileRedisTemplate.delete("analyzed:cv:" + taskId);
    }
}
