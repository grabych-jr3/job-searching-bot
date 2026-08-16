package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.service.SSENotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/tasks")
public class SseController {

    private final SSENotificationService sseNotificationService;

    public SseController(SSENotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/{taskId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOffers(@PathVariable String taskId){
        return sseNotificationService.subscribe(taskId);
    }
}
