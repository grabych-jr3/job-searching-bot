package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.analyzer_service.model.event.JobOfferEvent;
import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KafkaConsumerListener {

    private static final int BUFFER_MAX_SIZE = 20;
    private final Map<String, List<JobOffer>> buffers = new ConcurrentHashMap<>();

    private final AiAnalyzerService analyzerService;
    private final KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService;

    public KafkaConsumerListener(AiAnalyzerService analyzerService, KafkaProducerService<AnalyzedOfferEvent> kafkaProducerService) {
        this.analyzerService = analyzerService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @KafkaListener(topics = "found-offers-topic")
    public void consume(JobOfferEvent event){
        if (event.type() == JobOfferEvent.EventType.SEARCH_FINISHED){
            System.out.println("SEARCH FINISHED EVENT");
        }
        String taskId = event.taskId();

        if(event.type() == JobOfferEvent.EventType.OFFER){
            List<JobOffer> taskBuffer = buffers.computeIfAbsent(taskId, k -> new ArrayList<>());
            taskBuffer.add(event.offer());

            if (taskBuffer.size() >= BUFFER_MAX_SIZE){
                flushBuffer(taskId);
            }
        } else{
            flushBuffer(taskId);
            buffers.remove(taskId);
            kafkaProducerService.sendToKafka("completed-offer-topic", AnalyzedOfferEvent.finished(taskId));
        }
    }

    private void flushBuffer(String taskId){
        List<JobOffer> taskBuffer = buffers.get(taskId);

        if (taskBuffer != null && !taskBuffer.isEmpty()){
            List<JobOffer> batchToSend = new ArrayList<>(taskBuffer);

            analyzerService.analyze(taskId, batchToSend);
            taskBuffer.clear();
        }
    }
}
