package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.repository.AnalyzedOfferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyzedOfferService {

    private final AnalyzedOfferRepository analyzedOfferRepository;

    public AnalyzedOfferService(AnalyzedOfferRepository analyzedOfferRepository) {
        this.analyzedOfferRepository = analyzedOfferRepository;
    }

    @Transactional(readOnly = true)
    public Page<AnalyzedOffer> getCustomerHistory(Long customerId, Pageable pageable){
        return analyzedOfferRepository.findByCustomerId(customerId, pageable);
    }

    @Transactional
    public void saveAnalyzedOffer(AnalyzedOfferEvent offerEvent){
        analyzedOfferRepository.save(toAnalyzedOffer(offerEvent));
    }

    private AnalyzedOffer toAnalyzedOffer(AnalyzedOfferEvent offerEvent){
        OfferResult offerResult = offerEvent.offerResult();

        return AnalyzedOffer.builder()
                .offerUrl(offerResult.url())
                .jobTitle(offerResult.jobTitle())
                .score(offerResult.score())
                .reason(offerResult.reason())
                .customerId(offerEvent.customerId())
                .build();
    }
}
