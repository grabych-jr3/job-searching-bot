package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.service.AnalyzedOfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnalyzedOfferController {

    private final AnalyzedOfferService analyzedOfferService;

    public AnalyzedOfferController(AnalyzedOfferService analyzedOfferService) {
        this.analyzedOfferService = analyzedOfferService;
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AnalyzedOffer>> getCustomerHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "analyzedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalyzedOffer> history = analyzedOfferService.getCustomerHistory(userDetails.getCustomerId(), pageable);
        return ResponseEntity.ok(history);
    }
}
