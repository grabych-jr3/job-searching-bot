package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.model.event.JobOfferEvent;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;
    private final KafkaProducerService<JobOfferEvent> kafkaProducerService;

    public JobSearchService(List<JobSearcher> jobSearchers, KafkaProducerService<JobOfferEvent> kafkaProducerService) {
        this.jobSearchers = jobSearchers;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void searchAll(CreatedTaskEvent event){
        for (JobSearcher jobSearcher : jobSearchers){
            jobSearcher.search(
                    event,
                    offer -> kafkaProducerService.sendToKafka(
                            "found-offers-topic",
                            event.taskId(),
                            JobOfferEvent.offer(event.taskId(), event.customerId(), offer)
                    ));
        }
        kafkaProducerService.sendToKafka(
                "found-offers-topic",
                event.taskId(),
                JobOfferEvent.finishedOffer(event.taskId(), event.customerId())
        );
    }
}
