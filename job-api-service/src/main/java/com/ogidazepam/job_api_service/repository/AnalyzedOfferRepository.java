package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzedOfferRepository extends JpaRepository<AnalyzedOffer, Long> {
    Page<AnalyzedOffer> findByCustomerId(Long customerId, Pageable pageable);
}
