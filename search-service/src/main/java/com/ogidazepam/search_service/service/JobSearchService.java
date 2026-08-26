package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.model.event.JobOfferEvent;
import com.ogidazepam.search_service.strategy.JobSearcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;
    private final KafkaProducerService<JobOfferEvent> kafkaProducerService;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public JobSearchService(List<JobSearcher> jobSearchers, KafkaProducerService<JobOfferEvent> kafkaProducerService) {
        this.jobSearchers = jobSearchers;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void searchAll(CreatedTaskEvent event){
        try {
            List<CompletableFuture<Void>> futures = jobSearchers.stream()
                    .map(searcher -> CompletableFuture.runAsync(() -> {
                        try {
                            searcher.search(event, offer ->
                                    kafkaProducerService.sendToKafka(
                                            "found-offers-topic",
                                            event.taskId(),
                                            JobOfferEvent.offer(event.taskId(), event.customerId(), event.cvHash(), offer))
                            );
                        } catch (Exception e){
                            log.error("Scraper {} failed for task {}", searcher.getClass().getSimpleName(), event.taskId(), e);
                        }
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            kafkaProducerService.sendToKafka(
                    "found-offers-topic",
                    event.taskId(),
                    JobOfferEvent.finishedOffer(event.taskId(), event.customerId(), event.cvHash())
            );
        }
    }
}
