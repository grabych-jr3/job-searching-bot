package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.event.CreatedTaskEvent;
import com.ogidazepam.job_api_service.model.request.AnalyzeRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TaskService {

    public CreatedTaskEvent createTaskEvent(AnalyzeRequest analyzeRequest, byte[] fileBytes){
        return CreatedTaskEvent.builder()
                .taskId(generateTaskId())
                .cvHash(hashFile(fileBytes))
                .analyzeRequest(analyzeRequest)
                .build();
    }

    private String generateTaskId(){
        return UUID.randomUUID().toString();
    }

    private String hashFile(byte[] bytes){
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            return HexFormat.of().formatHex(messageDigest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm must be supported by JVM", e);
        }
    }
}
