package com.ogidazepam.search_service.websites.pracujpl.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.utils.RedisCacheService;
import com.ogidazepam.search_service.websites.pracujpl.client.PracujPlClient;
import com.ogidazepam.search_service.websites.pracujpl.mapper.PracujPlOfferMapper;
import com.ogidazepam.search_service.websites.pracujpl.model.offer.*;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.pracujpl.util.PracujPlUriBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Service
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;
    private final PracujPlUriBuilder uriBuilder;
    private final RedisCacheService redisCacheService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore semaphore = new Semaphore(6);

    public PracujPlJobSearcher(PracujPlClient pracujPlClient, PracujPlOfferMapper offerMapper, PracujPlUriBuilder uriBuilder, RedisCacheService redisCacheService) {
        this.pracujPlClient = pracujPlClient;
        this.offerMapper = offerMapper;
        this.uriBuilder = uriBuilder;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        List<String> urls = pracujPlClient.fetchOffersUrls(uri);

        List<CompletableFuture<Void>> tasks = urls.stream()
                .map(url -> CompletableFuture.runAsync(() -> {
                    JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(url);
                    if (cachedJobOffer != null){
                        onFoundJob.accept(cachedJobOffer);
                        return;
                    }

                    try {
                        semaphore.acquire();
                        PracujPlOfferData offerData = pracujPlClient.fetchOffer(url);
                        JobOffer jobOffer = offerMapper.mapToJobOffer(offerData, event.taskId());
                        onFoundJob.accept(jobOffer);
                        redisCacheService.writeJobOfferToCache(jobOffer);
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }
                    }, executor)
                )
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
}
