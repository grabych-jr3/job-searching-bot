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

    @Query("""
        SELECT a FROM AnalyzedOffer a
        WHERE (:minScore IS NULL OR a.score >= :minScore)
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
            Pageable pageable
    );

    @Modifying
    @Query(value = """
        INSERT INTO analyzed_offer (offer_url, cv_hash, job_title, company_name, reason, score, analyzed_at)
        VALUES (:offerUrl, :cvHash, :jobTitle, :companyName, :reason, :score, NOW())
        ON CONFLICT (cv_hash, offer_url) DO NOTHING
    """, nativeQuery = true)
    void insertIfNotExists(
            @Param("offerUrl") String offerUrl,
            @Param("cvHash") String cvHash,
            @Param("jobTitle") String jobTitle,
            @Param("companyName") String companyName,
            @Param("reason") String reason,
            @Param("score") int score
    );
}
