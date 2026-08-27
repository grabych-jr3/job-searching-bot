package com.ogidazepam.job_api_service.util;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public record ExceptionModel(
        String message,
        OffsetDateTime timestamp,
        HttpStatus status,
        String path,
        Map<String, String> fieldErrors
) {

    public static ExceptionModel of(String message, HttpStatus status, String path){
        return new ExceptionModel(message, OffsetDateTime.now(), status, path, null);
    }
}
