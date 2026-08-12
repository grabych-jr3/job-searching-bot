package com.ogidazepam.search_service.service;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSearchService {

    private final List<JobSearcher> jobSearchers;
    private final KafkaProducerService kafkaProducerService;
    private final RedisTemplate<String, Boolean> redisTemplate;

    public JobSearchService(List<JobSearcher> jobSearchers, KafkaProducerService kafkaProducerService, RedisTemplate<String, Boolean> redisTemplate) {
        this.jobSearchers = jobSearchers;
        this.kafkaProducerService = kafkaProducerService;
        this.redisTemplate = redisTemplate;
    }

    public List<JobOffer> searchAll(){
        List<JobOffer> offers = jobSearchers.stream()
                .flatMap(s -> s.search().stream())
                .toList();

        offers.forEach(offer -> {
            kafkaProducerService.sendToKafka(
                    "job-offer",
                    offer
            );

            redisTemplate.opsForValue().set(
                    "processed_offer:" + offer.source() + ":" + offer.id(),
                    true
            );
        });


        return offers;
    }
}
