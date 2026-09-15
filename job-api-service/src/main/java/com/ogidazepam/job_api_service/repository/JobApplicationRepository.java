package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.JobApplication;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import com.ogidazepam.job_api_service.model.projection.ApplicationStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByOfferUrl(String offerUrl);

    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    @Query("SELECT j.status AS status, COUNT(j) AS count FROM JobApplication j GROUP BY j.status")
    List<ApplicationStatusCount> countApplicationsByStatus();
}
