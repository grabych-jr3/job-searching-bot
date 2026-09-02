package com.ogidazepam.search_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String MAIN_TOPIC = "found-offers-topic";
    public static final String CONSUMING_TOPIC = "tasks-topic";

//    @Bean
//    public NewTopic jobTopic(){
//        return TopicBuilder
//                .name(MAIN_TOPIC)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
}