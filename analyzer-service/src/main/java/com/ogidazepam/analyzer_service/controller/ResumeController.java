package com.ogidazepam.analyzer_service.controller;

import com.ogidazepam.analyzer_service.model.candidate.CandidateProfile;
import com.ogidazepam.analyzer_service.service.AICandidateParser;
import com.ogidazepam.analyzer_service.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cv")
public class ResumeController {

    private final ResumeService resumeService;
    private final AICandidateParser aiCandidateParser;

    public ResumeController(ResumeService resumeService, AICandidateParser aiCandidateParser) {
        this.resumeService = resumeService;
        this.aiCandidateParser = aiCandidateParser;
    }

    @GetMapping
    public ResponseEntity<CandidateProfile> extractText(){
        return ResponseEntity.ok(aiCandidateParser.getOrCreateCandidateProfile("task"));
    }
}
