package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.exceptions.ResourceNotFoundException;
import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.repository.AnalyzedOfferRepository;
import com.ogidazepam.job_api_service.util.redis.AnalyzedOfferCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AnalyzedOfferService {

    private final AnalyzedOfferRepository analyzedOfferRepository;
    private final AnalyzedOfferCacheService analyzedOfferCacheService;

    public AnalyzedOfferService(AnalyzedOfferRepository analyzedOfferRepository, AnalyzedOfferCacheService analyzedOfferCacheService) {
        this.analyzedOfferRepository = analyzedOfferRepository;
        this.analyzedOfferCacheService = analyzedOfferCacheService;
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

    @Transactional
    public void deleteAll(){
        analyzedOfferRepository.deleteAll();
        log.info("Deleted all analyzed offer from DB");
        analyzedOfferCacheService.deleteAllAnalyzedOffersFromCache();
    }

    @Transactional
    public void deleteOffer(Long id){
        AnalyzedOffer analyzedOffer = analyzedOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offer with id " + id + " not found"));

        analyzedOfferRepository.delete(analyzedOffer);
        log.info("Deleted analyzed offer from DB: id={}, score={}, title=[{}], companyName=[{}], url=[{}]",
                analyzedOffer.getId(), analyzedOffer.getScore(), analyzedOffer.getJobTitle(), analyzedOffer.getCompanyName(), analyzedOffer.getOfferUrl());
        analyzedOfferCacheService.deleteOfferFromCache(analyzedOffer.getCvHash(), analyzedOffer.getOfferUrl());
    }
}
