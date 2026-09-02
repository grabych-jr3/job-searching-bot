package com.ogidazepam.job_api_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String MAIN_TOPIC = "tasks-topic";
    public static final String CONSUMING_TOPIC = "completed-offers-topic";

//    @Bean
//    public NewTopic searchJobTopics(){
//        return TopicBuilder
//                .name(MAIN_TOPIC)
//                .partitions(3)
//                .replicas(1)
//                .build();
//    }
}
