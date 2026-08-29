package com.ogidazepam.search_service.websites.pracujpl.service;

import com.ogidazepam.search_service.exception.*;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.utils.RedisCacheService;
import com.ogidazepam.search_service.websites.pracujpl.client.PracujPlClient;
import com.ogidazepam.search_service.websites.pracujpl.mapper.PracujPlOfferMapper;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.*;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.pracujpl.util.PracujPlUriBuilder;
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
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;
    private final PracujPlUriBuilder uriBuilder;
    private final RedisCacheService redisCacheService;

    private final Semaphore semaphore = new Semaphore(2);

    public PracujPlJobSearcher(PracujPlClient pracujPlClient, PracujPlOfferMapper offerMapper, PracujPlUriBuilder uriBuilder, RedisCacheService redisCacheService) {
        this.pracujPlClient = pracujPlClient;
        this.offerMapper = offerMapper;
        this.uriBuilder = uriBuilder;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        List<String> urls = fetchOffersUrls(uri);
        if (urls.isEmpty()){
            return;
        }

        try(ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> tasks = urls.stream()
                    .map(url -> CompletableFuture.runAsync(
                            () -> processOfferUrl(url, event.taskId(), onFoundJob),
                            executor
                    ))
                    .toList();

            CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
        }
    }

    private List<String> fetchOffersUrls(String uri){
        try {
            return pracujPlClient.fetchOffersUrls(uri);
        } catch (ScraperBlockedException e){
            log.error("PracujPl search blocked by anti-bot protection: {}", e.getMessage());
        } catch (Exception e){
            log.error("Failed to fetch PracujPl offers list from {}: {}", uri, e.getMessage());
        }
        return Collections.emptyList();
    }

    private void processOfferUrl(String url, String taskId, Consumer<JobOffer> onFoundJob){
        JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(url);
        if (cachedJobOffer != null){
            onFoundJob.accept(cachedJobOffer);
            return;
        }

        try {
            semaphore.acquire();
            PracujPlOfferData offerData = pracujPlClient.fetchOffer(url);
            JobOffer jobOffer = offerMapper.mapToJobOffer(offerData, taskId);
            onFoundJob.accept(jobOffer);
            redisCacheService.writeJobOfferToCache(jobOffer);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            log.warn("Processing interrupted for offer {}", url);
        } catch (Exception e){
            ErrorHandler.handleFetchError(url, e);
        } finally {
            semaphore.release();
        }
    }

}
