package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.model.event.JobOfferEvent;
import com.ogidazepam.search_service.strategy.JobSearcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;
    private final KafkaProducerService<JobOfferEvent> kafkaProducerService;

    public JobSearchService(List<JobSearcher> jobSearchers, KafkaProducerService<JobOfferEvent> kafkaProducerService) {
        this.jobSearchers = jobSearchers;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void searchAll(CreatedTaskEvent event){
        try {
            for (JobSearcher jobSearcher : jobSearchers){
                try {
                    jobSearcher.search(
                            event,
                            offer -> kafkaProducerService.sendToKafka(
                                    "found-offers-topic",
                                    event.taskId(),
                                    JobOfferEvent.offer(event.taskId(), event.customerId(), event.cvHash(), offer)
                            ));
                } catch (Exception e){
                    log.error("Scraper {} failed for task {}", jobSearcher.getClass().getSimpleName(), event.taskId(), e);
                }

            }
        } finally {
            kafkaProducerService.sendToKafka(
                    "found-offers-topic",
                    event.taskId(),
                    JobOfferEvent.finishedOffer(event.taskId(), event.customerId(), event.cvHash())
            );
        }
    }
}
