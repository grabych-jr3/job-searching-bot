package com.ogidazepam.search_service.websites.bulldogJob.service;

import com.ogidazepam.search_service.exception.*;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.utils.RedisCacheService;
import com.ogidazepam.search_service.websites.bulldogJob.client.BulldogJobClient;
import com.ogidazepam.search_service.websites.bulldogJob.mapper.BulldogJobMapper;
import com.ogidazepam.search_service.websites.bulldogJob.model.BulldogJobNextData;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.bulldogJob.util.BulldogJobUriBuilder;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.PracujPlOfferData;
import com.ogidazepam.search_service.websites.util.ErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Slf4j
@Service
public class BulldogJobSearcher implements JobSearcher {

    private final BulldogJobMapper jobMapper;
    private final BulldogJobClient bulldogJobClient;
    private final BulldogJobUriBuilder uriBuilder;
    private final RedisCacheService redisCacheService;

    private final Semaphore semaphore = new Semaphore(2);

    public BulldogJobSearcher(BulldogJobMapper jobMapper, BulldogJobClient bulldogJobClient, BulldogJobUriBuilder uriBuilder, RedisCacheService redisCacheService) {
        this.jobMapper = jobMapper;
        this.bulldogJobClient = bulldogJobClient;
        this.uriBuilder = uriBuilder;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        List<String> ids = fetchOffersIds(uri);
        if (ids.isEmpty()){
            return;
        }

        try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> tasks = ids.stream()
                    .map(id -> CompletableFuture.runAsync(
                            () -> processOfferUrl(id, event.taskId(), onFoundJob),
                            executor
                    ))
                    .toList();

            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        }
    }

    private List<String> fetchOffersIds(String uri){
        try {
            return bulldogJobClient.fetchJobOfferIds(uri);
        } catch (ScraperBlockedException e){
            log.error("BulldogJob search blocked by anti-bot protection: {}", e.getMessage());
        } catch (Exception e){
            log.error("Failed to fetch BulldogJob offers list from {}: {}", uri, e.getMessage());
        }
        return Collections.emptyList();
    }

    private void processOfferUrl(String id, String taskId, Consumer<JobOffer> onFoundJob){
        JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(id);
        if (cachedJobOffer != null){
            onFoundJob.accept(cachedJobOffer);
            return;
        }

        try {
            semaphore.acquire();
            BulldogJobNextData offerData = bulldogJobClient.fetchJobOffer(id);
            JobOffer jobOffer = jobMapper.mapToJobOffer(offerData, taskId);
            onFoundJob.accept(jobOffer);
            redisCacheService.writeJobOfferToCache(jobOffer);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.warn("Processing interrupted for offer {}", id);
        } catch (Exception e){
            ErrorHandler.handleFetchError(id, e);
        } finally {
            semaphore.release();
        }
    }
}
