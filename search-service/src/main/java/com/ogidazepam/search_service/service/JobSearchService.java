package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.event.CreatedTaskEvent;
import com.ogidazepam.search_service.model.event.JobOfferEvent;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;
    private final KafkaProducerService<JobOfferEvent> kafkaProducerService;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public JobSearchService(List<JobSearcher> jobSearchers, KafkaProducerService<JobOfferEvent> kafkaProducerService, RedisTemplate<String, Boolean> redisTemplate) {
        this.jobSearchers = jobSearchers;
        this.kafkaProducerService = kafkaProducerService;
        this.redisTemplate = redisTemplate;
    }

        public void searchAll(CreatedTaskEvent event){
            for (JobSearcher jobSearcher : jobSearchers){
                jobSearcher.search(
                        event,
                        offer -> {
                            kafkaProducerService.sendToKafka(
                                    "found-offers-topic",
                                    JobOfferEvent.offer(event.taskId(), offer)
                            );

                            redisTemplate.opsForValue().set(
                                    "processed_offer:" + offer.source() + ":" + offer.id(),
                                    true
                            );
                        }
                );
            }

            kafkaProducerService.sendToKafka(
                    "found-offers-topic",
                    JobOfferEvent.finishedOffer(event.taskId())
            );
    }
}
