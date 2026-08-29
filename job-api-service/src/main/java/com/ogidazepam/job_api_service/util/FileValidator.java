package com.ogidazepam.job_api_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final String PDF_MAGIC_HEADER = "%PDF-";

    public void validatePdf(MultipartFile file){
        if (file == null || file.isEmpty()){
            log.warn("PDF validation failed: uploaded file is null or empty");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please upload a non-empty CV file");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES){
            log.warn("PDF validation failed: file size ({} bytes) exceeds 5MB limit", file.getSize());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds the 5MB limit.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")){
            log.warn("PDF validation failed: invalid content type [{}]", contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are supported.");
        }

        try (InputStream is = file.getInputStream()){
            byte[] header = new byte[5];
            int bytesRead = is.read(header);
            if (bytesRead < 5 || !new String(header, StandardCharsets.US_ASCII).equals(PDF_MAGIC_HEADER)){
                log.warn("PDF validation failed: file does not begin with '%PDF-' magic header");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The uploaded file is not a valid PDF document.");
            }
        } catch (IOException e) {
            log.error("PDF validation failed: error reading file stream: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read the uploaded file.");
        }

        log.debug("PDF validation passed successfully: name=[{}], size={} bytes", file.getOriginalFilename(), file.getSize());
    }
}
