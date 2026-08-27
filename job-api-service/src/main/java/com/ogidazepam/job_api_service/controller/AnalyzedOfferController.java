package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.service.AnalyzedOfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AnalyzedOfferController {

    private final AnalyzedOfferService analyzedOfferService;

    public AnalyzedOfferController(AnalyzedOfferService analyzedOfferService) {
        this.analyzedOfferService = analyzedOfferService;
    }

    @GetMapping("/history")
    public ResponseEntity<Page<AnalyzedOffer>> getCustomerHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "analyzedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalyzedOffer> history = analyzedOfferService.getCustomerHistory(
                userDetails.getCustomerId(), minScore, maxScore, search, pageable
        );
        return ResponseEntity.ok(history);
    }
}
