package com.ogidazepam.analyzer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic completedOfferTopic(){
        return TopicBuilder
                .name("completed-offer-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
