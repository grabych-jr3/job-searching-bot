package com.ogidazepam.search_service.controller;

import com.ogidazepam.search_service.justjoinit.service.JustJoinItJobSearcher;
import com.ogidazepam.search_service.model.JobOffer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/justjoinit")
public class JustJoinItController {

    private final JustJoinItJobSearcher justJoinItJobSearcher;

    public JustJoinItController(@Qualifier("justJoinItJobSearcher") JustJoinItJobSearcher justJoinItJobSearcher) {
        this.justJoinItJobSearcher = justJoinItJobSearcher;
    }

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAll(){
        return ResponseEntity.ok(justJoinItJobSearcher.search());
    }
}
