package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.service.AnalyzedOfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class AnalyzedOfferController {

    private final AnalyzedOfferService analyzedOfferService;

    public AnalyzedOfferController(AnalyzedOfferService analyzedOfferService) {
        this.analyzedOfferService = analyzedOfferService;
    }

    @GetMapping()
    public ResponseEntity<Page<AnalyzedOffer>> getCustomerHistory(
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "analyzedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AnalyzedOffer> history = analyzedOfferService.getCustomerHistory(
                minScore, maxScore, search, pageable
        );
        return ResponseEntity.ok(history);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteCustomerHistory(){
        analyzedOfferService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteOfferFromHistory(@PathVariable Long id){
        analyzedOfferService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
