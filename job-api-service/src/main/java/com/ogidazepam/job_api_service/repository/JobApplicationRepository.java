package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.JobApplication;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByOfferUrl(String offerUrl);

    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
