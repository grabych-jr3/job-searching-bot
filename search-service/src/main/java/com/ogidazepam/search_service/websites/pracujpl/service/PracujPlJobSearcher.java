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
public class PracujPlJobSearcher implements JobSearcher{

    private final PracujPlClient pracujPlClient;
    private final PracujPlOfferMapper offerMapper;
    private final PracujPlUriBuilder uriBuilder;
    private final RedisCacheService redisCacheService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
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

        List<String> urls;
        try {
            urls = pracujPlClient.fetchOffersUrls(uri);
        } catch (ScraperBlockedException e){
            log.error("PracujPl search blocked by anti-bot protection: {}", e.getMessage());
            return;
        } catch (Exception e){
            log.error("Failed to fetch PracujPl offers list from {}: {}", uri, e.getMessage());
            return;
        }

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
                    } catch (OfferNotFoundException e){
                        log.debug("Offer {} not found (expired/deleted), skipping", url);
                    } catch (ScraperBlockedException e){
                        log.warn("Scraper blocked while fetching offer {}: {}", url, e.getMessage());
                    } catch (ScraperRateLimitException e){
                        log.warn("Scraper banned while fetching offer {}: {}", url, e.getMessage());
                    } catch (ScraperUnavailableException e){
                        log.warn("The server was unavailable during fetching the offer {}: {}", url, e.getMessage());
                    } catch (ScraperParsingException e){
                        log.warn("Scraper failed to parse data from {}: {}", url, e.getMessage());
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    } catch (Exception e){
                        log.error("Unexpected error parsing offer {}: {}", url, e.getMessage());
                    } finally {
                        semaphore.release();
                    }
                    }, executor)
                )
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
}
