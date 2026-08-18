package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final JobSearchService jobSearchService;

    public KafkaConsumerService(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @KafkaListener(topics = "search-jobs-topic")
    public void consume(CreatedTaskEvent event){
        jobSearchService.searchAll(event);
    }
}
