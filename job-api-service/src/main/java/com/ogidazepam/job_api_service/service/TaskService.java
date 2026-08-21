package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import com.ogidazepam.job_api_service.model.request.AnalyzeRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {

    public CreatedTaskEvent createTaskEvent(AnalyzeRequest analyzeRequest, String username){
        return CreatedTaskEvent.builder()
                .taskId(generateTaskId())
                .username(username)
                .analyzeRequest(analyzeRequest)
                .build();
    }

    private String generateTaskId(){
        return UUID.randomUUID().toString();
    }
}
