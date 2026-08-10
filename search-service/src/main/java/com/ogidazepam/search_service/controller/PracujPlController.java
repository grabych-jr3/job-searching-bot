package com.ogidazepam.search_service.controller;

import com.ogidazepam.search_service.model.JobOffer;
import com.ogidazepam.search_service.strategy.JobSearcher;
import com.ogidazepam.search_service.websites.pracujpl.service.PracujPlJobSearcher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pracujpl")
public class PracujPlController {

    private final JobSearcher jobSearcher;

    public PracujPlController(@Qualifier("pracujPlJobSearcher") JobSearcher jobSearcher) {
        this.jobSearcher = jobSearcher;
    }

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAll(){
        List<JobOffer> offers = jobSearcher.search();
        return ResponseEntity.ok(offers);
    }
}
