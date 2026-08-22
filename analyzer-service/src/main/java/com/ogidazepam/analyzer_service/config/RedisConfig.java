package com.ogidazepam.analyzer_service.config;

import com.ogidazepam.analyzer_service.model.OfferResult;
import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, byte[]> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        return template;
    }

    @Bean
    public RedisTemplate<String, CandidateProfile> candidateRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, CandidateProfile> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(CandidateProfile.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, OfferResult> offerResultRedisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, OfferResult> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(OfferResult.class));
        return template;
    }
}
