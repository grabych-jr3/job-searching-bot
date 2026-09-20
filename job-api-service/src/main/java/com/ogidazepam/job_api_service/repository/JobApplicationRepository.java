package com.ogidazepam.job_api_service.repository;

import com.ogidazepam.job_api_service.model.entity.JobApplication;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import com.ogidazepam.job_api_service.model.projection.ApplicationStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    Optional<JobApplication> findByIdAndCustomerId(Long id, Long customerId);

    Optional<JobApplication> findByOfferUrl(String offerUrl);

    Page<JobApplication> findByCustomerIdAndStatus(Long customerId, ApplicationStatus status, Pageable pageable);

    Page<JobApplication> findAllByCustomerId(Long customerId, Pageable pageable);

    @Query("SELECT j.status AS status, COUNT(j) AS count FROM JobApplication j WHERE j.customerId = :customerId GROUP BY j.status")
    List<ApplicationStatusCount> countApplicationsByStatus(@Param("customerId") Long customerId);
}
