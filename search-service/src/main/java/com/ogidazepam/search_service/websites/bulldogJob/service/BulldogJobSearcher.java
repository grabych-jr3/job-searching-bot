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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
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

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
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

        List<String> ids;
        try {
            ids = bulldogJobClient.fetchJobOfferIds(uri);
        } catch (ScraperBlockedException e){
            log.error("BulldogJob search blocked by anti-bot protection: {}", e.getMessage());
            return;
        } catch (Exception e){
            log.error("Failed to fetch BulldogJob offers list from {}: {}", uri, e.getMessage());
            return;
        }

        List<CompletableFuture<Void>> tasks = ids.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(id);
                    if (cachedJobOffer != null){
                        onFoundJob.accept(cachedJobOffer);
                        return;
                    }

                    try {
                        semaphore.acquire();
                        BulldogJobNextData job = bulldogJobClient.fetchJobOffer(id);
                        JobOffer jobOffer = jobMapper.mapToJobOffer(job, event.taskId());

                        onFoundJob.accept(jobOffer);
                        redisCacheService.writeJobOfferToCache(jobOffer);
                    } catch (OfferNotFoundException e){
                        log.debug("Offer {} not found (expired/deleted), skipping", id);
                    } catch (ScraperBlockedException e){
                        log.warn("Scraper blocked while fetching offer {}: {}", id, e.getMessage());
                    } catch (ScraperRateLimitException e){
                        log.warn("Scraper banned while fetching offer {}: {}", id, e.getMessage());
                    } catch (ScraperUnavailableException e){
                        log.warn("The server was unavailable during fetching the offer {}: {}", id, e.getMessage());
                    } catch (ScraperParsingException e){
                        log.warn("Scraper failed to parse data from {}: {}", id, e.getMessage());
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    } catch (Exception e){
                        log.error("Unexpected error parsing offer {}: {}", id, e.getMessage());
                    } finally {
                        semaphore.release();
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
}
