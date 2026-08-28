package com.ogidazepam.analyzer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public final static String MAIN_TOPIC = "completed-offers-topic";
    public final static String CONSUMING_TOPIC = "found-offers-topic";

    @Bean
    public NewTopic completedOfferTopic(){
        return TopicBuilder
                .name(MAIN_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
