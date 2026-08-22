package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import com.ogidazepam.job_api_service.model.request.AnalyzeRequest;
import com.ogidazepam.job_api_service.service.KafkaProducerService;
import com.ogidazepam.job_api_service.service.TaskService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class JobController {

    private final TaskService taskService;
    private final RedisTemplate<String, byte[]> bytesRedisTemplate;
    private final KafkaProducerService<CreatedTaskEvent> kafkaProducerService;

    public JobController(TaskService taskService, RedisTemplate<String, byte[]> bytesRedisTemplate, KafkaProducerService<CreatedTaskEvent> kafkaProducerService) {
        this.taskService = taskService;
        this.bytesRedisTemplate = bytesRedisTemplate;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CreatedTaskEvent> analyzeOffers(
            AnalyzeRequest analyzeRequest,
            @RequestPart("file")MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) throws IOException {
        CreatedTaskEvent event = taskService.createTaskEvent(analyzeRequest, userDetails.getCustomerId());

        bytesRedisTemplate.opsForValue().set("cv:" + event.taskId(), file.getBytes(), Duration.ofHours(1));
        kafkaProducerService.sendToKafka("search-jobs-topic", event);

        return ResponseEntity
                .accepted()
                .body(event);
    }
}
