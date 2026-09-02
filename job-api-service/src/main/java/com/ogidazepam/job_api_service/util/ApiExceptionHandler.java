package com.ogidazepam.job_api_service.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionModel> handleDataIntegrity(DataIntegrityViolationException e,
                                                               HttpServletRequest request){
        log.warn("Data integrity conflict at path [{}]: {}", request.getRequestURI(), e.getMessage());
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionModel> handleMaxSize(MaxUploadSizeExceededException e,
                                                        HttpServletRequest request){
        log.warn("Max upload size exceeded at path [{}]: {}", request.getRequestURI(), e.getMessage());
        HttpStatus status = HttpStatus.CONTENT_TOO_LARGE;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ExceptionModel> handleMissingPart(MissingServletRequestPartException e,
                                                             HttpServletRequest request){
        log.warn("Missing request part at path [{}]: {}", request.getRequestURI(), e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionModel> handleResponseStatus(ResponseStatusException e,
                                                               HttpServletRequest request){
        log.warn("Response status exception at path [{}]: {}", request.getRequestURI(), e.getReason());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getReason(), status, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionModel> handleValidationException(MethodArgumentNotValidException e,
                                                                    HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : e.getFieldErrors()){
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("Validation failed for request to [{}]: {}", request.getRequestURI(), fieldErrors);

        ExceptionModel exceptionModel = new ExceptionModel(
                "Validation failed for one or more fields",
                OffsetDateTime.now(),
                status,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.status(status)
                .body(exceptionModel);
    }
}
