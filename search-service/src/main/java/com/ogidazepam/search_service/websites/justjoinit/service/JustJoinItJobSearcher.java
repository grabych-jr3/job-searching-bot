package com.ogidazepam.search_service.websites.justjoinit.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.utils.RedisCacheService;
import com.ogidazepam.search_service.websites.justjoinit.client.JustJoinItClient;
import com.ogidazepam.search_service.websites.justjoinit.mapper.JustJoinItMapper;
import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobData;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobDetails;
import com.ogidazepam.search_service.websites.justjoinit.model.JustJoinItJobOffer;
import com.ogidazepam.search_service.websites.justjoinit.util.JustJoinItUriBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

@Service
public class JustJoinItJobSearcher implements JobSearcher {


    private final JustJoinItMapper mapper;
    private final JustJoinItUriBuilder uriBuilder;
    private final JustJoinItClient justJoinItClient;
    private final RedisCacheService redisCacheService;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore semaphore = new Semaphore(6);

    public JustJoinItJobSearcher(JustJoinItMapper mapper, JustJoinItUriBuilder uriBuilder, JustJoinItClient justJoinItClient, RedisCacheService redisCacheService) {
        this.mapper = mapper;
        this.uriBuilder = uriBuilder;
        this.justJoinItClient = justJoinItClient;
        this.redisCacheService = redisCacheService;
    }

    @Override
    public void search(CreatedTaskEvent event, Consumer<JobOffer> onFoundJob) {
        String uri = uriBuilder.buildUri(event.analyzeRequest());

        List<JustJoinItJobOffer> offers = justJoinItClient.fetchJobOffers(uri);

        List<CompletableFuture<Void>> tasks = offers.stream()
                .map(offer -> CompletableFuture.runAsync(() -> {
                    JobOffer cachedJobOffer = redisCacheService.getJobOfferFromCache(offer.slug());
                    if (cachedJobOffer != null){
                        onFoundJob.accept(cachedJobOffer);
                        return;
                    }

                    try {
                        semaphore.acquire();
                        JustJoinItJobDetails details = justJoinItClient
                                .fetchJobOffersDetails(offer.slug());
                        JobOffer jobOffer = mapper.mapToJobOffer(new JustJoinItJobData(
                                offer,
                                        details
                                ),
                                event.taskId()
                        );
                        onFoundJob.accept(jobOffer);
                        redisCacheService.writeJobOfferToCache(jobOffer);
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    } finally {
                        semaphore.release();
                    }
                    }, executor))
                .toList();

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
    }
}
