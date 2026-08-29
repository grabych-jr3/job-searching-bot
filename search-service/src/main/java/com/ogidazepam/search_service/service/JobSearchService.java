package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.config.KafkaConfig;
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
        log.info("Starting concurrent search across {} scrapers for taskId: {}, tech: {}, exp: {}, workModes: {}",
                jobSearchers.size(), event.taskId(), event.analyzeRequest().technology(),
                event.analyzeRequest().experience(), event.analyzeRequest().workMode());

        try {
            List<CompletableFuture<Void>> futures = jobSearchers.stream()
                    .map(searcher -> CompletableFuture.runAsync(() -> {
                        String searcherName = searcher.getClass().getSimpleName();
                        try {
                            log.debug("Scraper [{}] starting for taskId [{}]", searcherName, event.taskId());
                            searcher.search(event, offer ->
                                    kafkaProducerService.sendToKafka(
                                            KafkaConfig.MAIN_TOPIC,
                                            event.taskId(),
                                            JobOfferEvent.offer(event.taskId(), event.customerId(), event.cvHash(), offer))
                            );
                            log.debug("Scraper [{}] finished for taskId [{}]", searcherName, event.taskId());
                        } catch (Exception e){
                            log.error("Scraper [{}] failed for taskId [{}]: {}", searcherName, event.taskId(), e.getMessage(), e);
                        }
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            log.info("Completed all scrapers for taskId: [{}]. Dispatching SEARCH_FINISHED event to Kafka.", event.taskId());
            kafkaProducerService.sendToKafka(
                    KafkaConfig.MAIN_TOPIC,
                    event.taskId(),
                    JobOfferEvent.finishedOffer(event.taskId(), event.customerId(), event.cvHash())
            );
        }
    }
}
