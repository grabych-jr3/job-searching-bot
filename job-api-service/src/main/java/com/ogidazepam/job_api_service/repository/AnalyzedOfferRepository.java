package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzedOfferRepository extends JpaRepository<AnalyzedOffer, Long> {
    Page<AnalyzedOffer> findByCustomerId(Long customerId, Pageable pageable);

    @Query("""
        SELECT a FROM AnalyzedOffer a
        WHERE a.customerId = :customerId
          AND (:minScore IS NULL OR a.score >= :minScore)
          AND (:maxScore IS NULL OR a.score <= :maxScore)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.reason) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """)
    Page<AnalyzedOffer> findByCustomerIdWithFilters(
            @Param("customerId") Long customerId,
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("search") String search,
            Pageable pageable
    );

    @Modifying
    @Query(value = """
        INSERT INTO analyzed_offer (customer_id, offer_url, cv_hash, job_title, reason, score, analyzed_at)
        VALUES (:customerId, :offerUrl, :cvHash, :jobTitle, :reason, :score, NOW())
        ON CONFLICT (customer_id, cv_hash, offer_url) DO NOTHING
    """, nativeQuery = true)
    void insertIfNotExists(
            @Param("customerId") Long customerId,
            @Param("offerUrl") String offerUrl,
            @Param("cvHash") String cvHash,
            @Param("jobTitle") String jobTitle,
            @Param("reason") String reason,
            @Param("score") int score
    );
}
