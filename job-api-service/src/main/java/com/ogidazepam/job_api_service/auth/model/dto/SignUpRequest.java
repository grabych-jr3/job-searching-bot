package com.ogidazepam.job_api_service.auth.model.dto;

public record SignUpRequest(
        String email,
        String password
) {
}
