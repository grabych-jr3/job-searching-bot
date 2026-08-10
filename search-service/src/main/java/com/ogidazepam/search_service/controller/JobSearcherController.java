package com.ogidazepam.search_service.controller;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.service.JobSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobSearcherController {

    private final JobSearchService jobSearchService;

    public JobSearcherController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping
    public ResponseEntity<List<JobOffer>> searchAll(){
        List<JobOffer> offers = jobSearchService.searchAll();
        return ResponseEntity.ok(offers);
    }
}
