package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {

    public CreatedTaskEvent createTaskEvent(){
        return CreatedTaskEvent.builder()
                .taskId(generateTaskId())
                .build();
    }

    private String generateTaskId(){
        return UUID.randomUUID().toString();
    }
}
