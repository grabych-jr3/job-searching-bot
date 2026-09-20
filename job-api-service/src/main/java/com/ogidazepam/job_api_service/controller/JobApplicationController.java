package com.ogidazepam.job_api_service.controller;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import com.ogidazepam.job_api_service.model.enums.ApplicationStatus;
import com.ogidazepam.job_api_service.model.request.ApplicationNotesRequest;
import com.ogidazepam.job_api_service.model.request.ApplyRequest;
import com.ogidazepam.job_api_service.model.response.ApplyResponse;
import com.ogidazepam.job_api_service.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<Void> applyForVacation(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @RequestBody @Valid ApplyRequest applyRequest){
        jobApplicationService.applyForVacancy(userDetails.getCustomerId(), applyRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeApplicationStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long id,
                                                        @RequestParam ApplicationStatus status){
        jobApplicationService.changeApplicationStatus(userDetails.getCustomerId(), id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/addNotes")
    public ResponseEntity<Void> addNotesToApplication(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody ApplicationNotesRequest notesRequest,
                                                      @PathVariable Long id){
        jobApplicationService.addNotesToApplication(userDetails.getCustomerId(), id, notesRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<ApplyResponse>> getApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<ApplyResponse> applyResponsePage = jobApplicationService.getApplication(userDetails.getCustomerId(), status, pageable);
        return ResponseEntity.ok(applyResponsePage);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<ApplicationStatus, Long>> getApplicationStats(@AuthenticationPrincipal CustomUserDetails userDetails){
        return ResponseEntity.ok(jobApplicationService.getApplicationStats(userDetails.getCustomerId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeApplication(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable Long id){
        jobApplicationService.removeApplication(userDetails.getCustomerId(), id);
        return ResponseEntity.noContent().build();
    }
}
