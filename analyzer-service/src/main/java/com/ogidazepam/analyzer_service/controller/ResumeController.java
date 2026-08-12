package com.ogidazepam.analyzer_service.controller;

import com.ogidazepam.analyzer_service.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cv")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping
    public ResponseEntity<String> extractText(){
        return ResponseEntity.ok(resumeService.extractTextFromPdf());
    }
}
