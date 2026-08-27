package com.ogidazepam.job_api_service.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class, AuthenticationException.class})
    public ResponseEntity<ExceptionModel> handleBadCredentials(Exception e, HttpServletRequest request){
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    private ResponseEntity<ExceptionModel> handleAccessDenied(AccessDeniedException e,
                                                              HttpServletRequest request){
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionModel> handleDataIntegrity(DataIntegrityViolationException e,
                                                               HttpServletRequest request){
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ExceptionModel> handleMaxSize(MaxUploadSizeExceededException e,
                                                        HttpServletRequest request){
        HttpStatus status = HttpStatus.CONTENT_TOO_LARGE;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ExceptionModel> handleMissingPart(MissingServletRequestPartException e,
                                                             HttpServletRequest request){
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ExceptionModel.of(e.getMessage(), status, request.getRequestURI()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionModel> handleResponseStatus(ResponseStatusException e,
                                                               HttpServletRequest request){
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
