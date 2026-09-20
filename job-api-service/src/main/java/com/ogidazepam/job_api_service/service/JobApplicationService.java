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

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    private final AnalyzedOfferRepository analyzedOfferRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public JobApplicationService(AnalyzedOfferRepository analyzedOfferRepository, JobApplicationRepository jobApplicationRepository) {
        this.analyzedOfferRepository = analyzedOfferRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional
    public void applyForVacancy(Long customerId, ApplyRequest applyRequest){
        JobApplication jobApplication = mapToJobApplication(applyRequest);
        jobApplication.setCustomerId(customerId);
        jobApplication.setStatus(ApplicationStatus.APPLIED);

        jobApplicationRepository.save(jobApplication);
    }

    @Transactional
    public void changeApplicationStatus(Long customerId, Long id, ApplicationStatus status){
        JobApplication jobApplication = jobApplicationRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication by id " + id + " and customerId " + customerId + " was not found"));

        jobApplication.setStatus(status);
        analyzedOfferRepository.changeStatus(status, jobApplication.getOfferUrl(), customerId);
    }

    @Transactional(readOnly = true)
    public Page<ApplyResponse> getApplication(Long customerId, ApplicationStatus status, Pageable pageable){
        Page<JobApplication> jobApplications = status != null
                ? jobApplicationRepository.findByCustomerIdAndStatus(customerId, status, pageable)
                : jobApplicationRepository.findAllByCustomerId(customerId, pageable);

        return jobApplications.map(this::mapToApplyResponse);
    }

    @Transactional(readOnly = true)
    public Map<ApplicationStatus, Long> getApplicationStats(Long customerId) {
        Map<ApplicationStatus, Long> stats = Arrays.stream(ApplicationStatus.values())
                .collect(Collectors.toMap(s -> s, s -> 0L, (a, b) -> a, () -> new EnumMap<>(ApplicationStatus.class)));

        jobApplicationRepository.countApplicationsByStatus(customerId)
                .forEach(res -> stats.put(res.getStatus(), res.getCount()));

        return stats;
    }

    @Transactional
    public void addNotesToApplication(Long customerId, Long id, ApplicationNotesRequest request) {
        JobApplication jobApplication = jobApplicationRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication by id " + id + " and customerId " + customerId + " was not found"));

        jobApplication.setNotes(request.notes());
    }

    @Transactional
    public void removeApplication(Long customerId, Long id){
        JobApplication jobApplication = jobApplicationRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication by id " + id + " and customerId " + customerId + " was not found"));

        jobApplicationRepository.delete(jobApplication);
        analyzedOfferRepository.changeStatus(ApplicationStatus.ACTIVE, jobApplication.getOfferUrl(), customerId);
    }

    private JobApplication mapToJobApplication(ApplyRequest applyRequest){
        return JobApplication.builder()
                .offerUrl(applyRequest.offerUrl())
                .jobTitle(applyRequest.jobTitle())
                .companyName(applyRequest.companyName())
                .notes(applyRequest.notes())
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
