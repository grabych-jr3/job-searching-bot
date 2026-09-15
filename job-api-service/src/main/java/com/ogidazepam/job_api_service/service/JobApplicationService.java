package com.ogidazepam.job_api_service.service;

import com.ogidazepam.job_api_service.exceptions.ResourceNotFoundException;
import com.ogidazepam.job_api_service.model.entity.JobApplication;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import com.ogidazepam.job_api_service.model.request.ApplicationNotesRequest;
import com.ogidazepam.job_api_service.model.request.ApplyRequest;
import com.ogidazepam.job_api_service.model.response.ApplyResponse;
import com.ogidazepam.job_api_service.repository.AnalyzedOfferRepository;
import com.ogidazepam.job_api_service.repository.JobApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    private final AnalyzedOfferRepository analyzedOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobApplicationService(AnalyzedOfferRepository analyzedOfferRepository, JobApplicationRepository jobApplicationRepository) {
        this.analyzedOfferRepository = analyzedOfferRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional
    public void applyForVacancy(ApplyRequest applyRequest){
        JobApplication jobApplication = mapToJobApplication(applyRequest);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        jobApplicationRepository.save(jobApplication);
    }

    @Transactional
    public void changeApplicationStatus(Long id, ApplicationStatus status){
        JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication by id " + id + " was not found"));

        jobApplication.setStatus(status);
        analyzedOfferRepository.changeStatus(status, jobApplication.getOfferUrl());
    }

    @Transactional(readOnly = true)
    public Page<ApplyResponse> getApplication(ApplicationStatus status, Pageable pageable){
        Page<JobApplication> jobApplications = status != null
                ? jobApplicationRepository.findByStatus(status, pageable)
                : jobApplicationRepository.findAll(pageable);

        return jobApplications.map(this::mapToApplyResponse);
    }

    @Transactional
    public void addNotesToApplication(Long id, ApplicationNotesRequest request) {
        JobApplication jobApplication = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication by id " + id + " was not found"));

        jobApplication.setNotes(request.notes());
    }

    private JobApplication mapToJobApplication(ApplyRequest applyRequest){
        return JobApplication.builder()
                .offerUrl(applyRequest.offerUrl())
                .jobTitle(applyRequest.jobTitle())
                .companyName(applyRequest.companyName())
                .build();
    }

    private ApplyResponse mapToApplyResponse(JobApplication jobApplication){
        return ApplyResponse.builder()
                .id(jobApplication.getId())
                .jobTitle(jobApplication.getJobTitle())
                .companyName(jobApplication.getCompanyName())
                .offerUrl(jobApplication.getOfferUrl())
                .notes(jobApplication.getNotes())
                .status(jobApplication.getStatus())
                .appliedAt(jobApplication.getAppliedAt())
                .build();
    }
}
