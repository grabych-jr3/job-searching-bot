package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.AnalyzedOffer;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyzedOfferRepository extends JpaRepository<AnalyzedOffer, Long> {

    Page<AnalyzedOffer> findAllByCustomerId(Long customerId, Pageable pageable);

    void deleteAllByCustomerId(Long id);

    Optional<AnalyzedOffer> findByIdAndCustomerId(Long id, Long customerId);

    @Query("""
        SELECT a FROM AnalyzedOffer a
        WHERE a.customerId = :customerId
          AND (:minScore IS NULL OR a.score >= :minScore)
          AND (:maxScore IS NULL OR a.score <= :maxScore)
          AND (
            :search IS NULL OR :search = ''
            OR LOWER(a.jobTitle) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.reason) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(a.companyName) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """)
    Page<AnalyzedOffer> findWithFilters(
            @Param("minScore") Integer minScore,
            @Param("maxScore") Integer maxScore,
            @Param("search") String search,
            @Param("customerId") Long customerId,
            Pageable pageable
    );

    @Modifying
    @Query(value = """
        INSERT INTO analyzed_offer (customer_id, offer_url, cv_hash, job_title, company_name, reason, score, status, analyzed_at)
        VALUES (:customerId, :offerUrl, :cvHash, :jobTitle, :companyName, :reason, :score, :status, NOW())
        ON CONFLICT (cv_hash, offer_url) DO NOTHING
    """, nativeQuery = true)
    void insertIfNotExists(
            @Param("customerId") Long customerId,
            @Param("offerUrl") String offerUrl,
            @Param("cvHash") String cvHash,
            @Param("jobTitle") String jobTitle,
            @Param("companyName") String companyName,
            @Param("reason") String reason,
            @Param("score") int score,
            @Param("status") String status
    );

    @Modifying
    @Query("""
    UPDATE AnalyzedOffer SET status = :status WHERE offerUrl = :offerUrl AND customerId = :customerId
    """)
    void changeStatus(
            @Param("status") ApplicationStatus status,
            @Param("offerUrl") String offerUrl,
            @Param("customerId") Long customerId
    );
}
