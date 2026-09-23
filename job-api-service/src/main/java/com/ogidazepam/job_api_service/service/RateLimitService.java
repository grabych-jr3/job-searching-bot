package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.auth.model.enums.SubscriptionTier;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final LettuceBasedProxyManager<String> proxyManager;

    public RateLimitService(LettuceBasedProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key, SubscriptionTier tier){
        BucketConfiguration configuration = getBucketConfigurationForTier(tier);

        return proxyManager.builder()
                .build(key, () -> configuration);
    }

    private BucketConfiguration getBucketConfigurationForTier(SubscriptionTier tier){
        return switch (tier) {
            case PREMIUM -> BucketConfiguration.builder()
                    .addLimit(limit -> limit.capacity(50).refillGreedy(50, Duration.ofDays(1)))
                    .build();
            case null, default -> BucketConfiguration.builder()
                    .addLimit(limit -> limit.capacity(1).refillGreedy(1, Duration.ofMinutes(1)))
                    .addLimit(limit -> limit.capacity(7).refillGreedy(7, Duration.ofDays(1)))
                    .build();
        };
    }
}
