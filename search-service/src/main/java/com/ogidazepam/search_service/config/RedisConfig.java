package com.ogidazepam.search_service.config;

import com.ogidazepam.search_service.model.JobOffer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, JobOffer> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, JobOffer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(JobOffer.class));
        return template;
    }
}
