package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import com.ogidazepam.job_api_service.service.KafkaProducerService;
import com.ogidazepam.job_api_service.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class JobController {

    private final TaskService taskService;
    private final KafkaProducerService<CreatedTaskEvent> kafkaProducerService;

    public JobController(TaskService taskService, KafkaProducerService<CreatedTaskEvent> kafkaProducerService) {
        this.taskService = taskService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<CreatedTaskEvent> analyzeOffers(){
        CreatedTaskEvent event = taskService.createTaskEvent();

        kafkaProducerService.sendToKafka("search-jobs-topic", event);

        return ResponseEntity
                .accepted()
                .body(event);
    }
}
