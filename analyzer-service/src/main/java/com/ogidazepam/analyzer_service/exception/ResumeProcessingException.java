package com.ogidazepam.analyzer_service.exception;

public class ResumeProcessingException extends RuntimeException {
    public ResumeProcessingException(String message) {
        super(message);
    }

    public ResumeProcessingException(String message, Throwable cause){
        super(message, cause);
    }
}
