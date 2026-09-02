package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.config.KafkaConfig;
import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import com.ogidazepam.job_api_service.model.request.AnalyzeRequest;
import com.ogidazepam.job_api_service.service.KafkaProducerService;
import com.ogidazepam.job_api_service.service.TaskService;
import com.ogidazepam.job_api_service.util.FileValidator;
import com.ogidazepam.job_api_service.util.redis.CvBytesCacheService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api")
public class JobController {

    private final TaskService taskService;
    private final FileValidator fileValidator;
    private final CvBytesCacheService cvBytesCacheService;
    private final KafkaProducerService<CreatedTaskEvent> kafkaProducerService;

    public JobController(TaskService taskService, FileValidator fileValidator, CvBytesCacheService cvBytesCacheService, KafkaProducerService<CreatedTaskEvent> kafkaProducerService) {
        this.taskService = taskService;
        this.fileValidator = fileValidator;
        this.cvBytesCacheService = cvBytesCacheService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CreatedTaskEvent> analyzeOffers(
            @Valid AnalyzeRequest analyzeRequest,
            @RequestPart("file")MultipartFile file
            ) throws IOException {
        log.info("Received analysis request: technology={}, experience={}, workModes={}, fileSize={} bytes",
                analyzeRequest.technology(), analyzeRequest.experience(), analyzeRequest.workMode(), file.getSize());

        fileValidator.validatePdf(file);
        CreatedTaskEvent event = taskService.createTaskEvent(analyzeRequest, file.getBytes());

        cvBytesCacheService.cacheCvBytes(event.taskId(), file.getBytes());
        kafkaProducerService.sendToKafka(KafkaConfig.MAIN_TOPIC, event.taskId(), event);

        log.info("Created and dispatched analysis task [{}]", event.taskId());

        return ResponseEntity
                .accepted()
                .body(event);
    }
}
