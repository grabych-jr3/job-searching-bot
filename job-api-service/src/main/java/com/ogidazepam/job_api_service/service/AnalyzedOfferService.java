package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.exceptions.ResourceNotFoundException;
import com.ogidazepam.job_api_service.model.OfferResult;
import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.model.entity.JobApplication;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import com.ogidazepam.job_api_service.model.event.AnalyzedOfferEvent;
import com.ogidazepam.job_api_service.repository.AnalyzedOfferRepository;
import com.ogidazepam.job_api_service.repository.JobApplicationRepository;
import com.ogidazepam.job_api_service.util.redis.AnalyzedOfferCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Slf4j
@Service
public class AnalyzedOfferService {

    private final AnalyzedOfferRepository analyzedOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AnalyzedOfferCacheService analyzedOfferCacheService;

    public AnalyzedOfferService(AnalyzedOfferRepository analyzedOfferRepository, JobApplicationRepository jobApplicationRepository, AnalyzedOfferCacheService analyzedOfferCacheService) {
        this.analyzedOfferRepository = analyzedOfferRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.analyzedOfferCacheService = analyzedOfferCacheService;
    }

    @Transactional(readOnly = true)
    public Page<AnalyzedOffer> getCustomerHistory(Long customerId, Integer minScore, Integer maxScore, String search, Pageable pageable){
        log.debug("Querying history for minScore={}, maxScore={}, search=[{}], page={}, size={}",
                minScore, maxScore, search, pageable.getPageNumber(), pageable.getPageSize());

        if (minScore != null || maxScore != null || (search != null && !search.isBlank())) {
            return analyzedOfferRepository.findWithFilters(
                    minScore, maxScore, search != null ? search.trim() : null, customerId, pageable
            );
        }
        return analyzedOfferRepository.findAllByCustomerId(customerId, pageable);
    }

    @Transactional
    public void saveAnalyzedOffer(AnalyzedOfferEvent offerEvent){
        OfferResult offerResult = offerEvent.offerResult();
        log.info("Persisting analyzed offer to DB: taskId=[{}], score={}, title=[{}], companyName=[{}], url=[{}]",
                 offerEvent.taskId(), offerResult.score(), offerResult.jobTitle(), offerResult.companyName(), offerResult.url());

        Optional<JobApplication> jobApplicationOptional = jobApplicationRepository.findByOfferUrl(offerResult.url());

        if (jobApplicationOptional.isPresent()){
            analyzedOfferRepository.insertIfNotExists(
                    offerEvent.customerId(),
                    offerResult.url(),
                    offerEvent.cvHash(),
                    offerResult.jobTitle(),
                    offerResult.companyName(),
                    offerResult.reason(),
                    offerResult.score(),
                    jobApplicationOptional.get().getStatus().name()
            );
        } else {
            analyzedOfferRepository.insertIfNotExists(
                    offerEvent.customerId(),
                    offerResult.url(),
                    offerEvent.cvHash(),
                    offerResult.jobTitle(),
                    offerResult.companyName(),
                    offerResult.reason(),
                    offerResult.score(),
                    ApplicationStatus.ACTIVE.name()
            );
        }
    }

    @Transactional
    public void deleteAll(Long customerId){
        analyzedOfferRepository.deleteAllByCustomerId(customerId);
        log.info("Deleted all analyzed offer from DB");
        analyzedOfferCacheService.deleteAllAnalyzedOffersFromCache(customerId);
    }

    @Transactional
    public void deleteOffer(Long customerId, Long id){
        AnalyzedOffer analyzedOffer = analyzedOfferRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer with id " + id + " not found"));

        analyzedOfferRepository.delete(analyzedOffer);
        log.info("Deleted analyzed offer from DB: id={}, score={}, title=[{}], companyName=[{}], url=[{}]",
                analyzedOffer.getId(), analyzedOffer.getScore(), analyzedOffer.getJobTitle(), analyzedOffer.getCompanyName(), analyzedOffer.getOfferUrl());
        analyzedOfferCacheService.deleteOfferFromCache(customerId, analyzedOffer.getCvHash(), analyzedOffer.getOfferUrl());
    }

    @Transactional
    public void markAsApplied(Long customerId, String offerUrl){
        analyzedOfferRepository.changeStatus(ApplicationStatus.APPLIED, offerUrl, customerId);
    }
}
