package com.ogidazepam.analyzer_service.service;

import com.ogidazepam.analyzer_service.model.offer.JobOffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KafkaConsumerListener {

    private static final int BUFFER_MAX_SIZE = 20;
    private final List<JobOffer> buffer = new ArrayList<>();

    private final AiAnalyzerService analyzerService;

    public KafkaConsumerListener(AiAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @KafkaListener(topics = "job-offer")
    public void consume(JobOffer offer){
        buffer.add(offer);

        if (buffer.size() >= BUFFER_MAX_SIZE){
            flushBuffer();
        }
    }

    private void flushBuffer(){
        List<JobOffer> batchToSend = new ArrayList<>(buffer);
        analyzerService.analyze(batchToSend);
        buffer.clear();
    }
}
