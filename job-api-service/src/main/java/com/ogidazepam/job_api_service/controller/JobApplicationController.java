package com.ogidazepam.job_api_service.controller;

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
    public ResponseEntity<Void> applyForVacation(@RequestBody @Valid ApplyRequest applyRequest){
        jobApplicationService.applyForVacancy(applyRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/change-status")
    public ResponseEntity<Void> changeApplicationStatus(@PathVariable Long id,
                                                        @RequestParam ApplicationStatus status){
        jobApplicationService.changeApplicationStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/addNotes")
    public ResponseEntity<Void> addNotesToApplication(@RequestBody ApplicationNotesRequest notesRequest,
                                                      @PathVariable Long id){
        jobApplicationService.addNotesToApplication(id, notesRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<ApplyResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<ApplyResponse> applyResponsePage = jobApplicationService.getApplication(status, pageable);
        return ResponseEntity.ok(applyResponsePage);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<ApplicationStatus, Long>> getApplicationStats(){
        return ResponseEntity.ok(jobApplicationService.getApplicationStats());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeApplication(@PathVariable Long id){
        jobApplicationService.removeApplication(id);
        return ResponseEntity.noContent().build();
    }
}
