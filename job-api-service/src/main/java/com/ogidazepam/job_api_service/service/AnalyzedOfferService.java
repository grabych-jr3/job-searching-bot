package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.repository.AnalyzedOfferRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AnalyzedOfferService {

    private final AnalyzedOfferRepository analyzedOfferRepository;

    public AnalyzedOfferService(AnalyzedOfferRepository analyzedOfferRepository) {
        this.analyzedOfferRepository = analyzedOfferRepository;
    }

    @Transactional(readOnly = true)
    public Page<AnalyzedOffer> getCustomerHistory(Integer minScore, Integer maxScore, String search, Pageable pageable){
        log.debug("Querying history for minScore={}, maxScore={}, search=[{}], page={}, size={}",
                minScore, maxScore, search, pageable.getPageNumber(), pageable.getPageSize());

        if (minScore != null || maxScore != null || (search != null && !search.isBlank())) {
            return analyzedOfferRepository.findWithFilters(
                    minScore, maxScore, search != null ? search.trim() : null, pageable
            );
        }
        return analyzedOfferRepository.findAll(pageable);
    }

    @Transactional
    public void saveAnalyzedOffer(AnalyzedOfferEvent offerEvent){
        OfferResult offerResult = offerEvent.offerResult();
        log.info("Persisting analyzed offer to DB: taskId=[{}], score={}, title=[{}], companyName=[{}], url=[{}]",
                 offerEvent.taskId(), offerResult.score(), offerResult.jobTitle(), offerResult.companyName(), offerResult.url());

        analyzedOfferRepository.insertIfNotExists(
                offerResult.url(),
                offerEvent.cvHash(),
                offerResult.jobTitle(),
                offerResult.companyName(),
                offerResult.reason(),
                offerResult.score()
        );
    }
}
