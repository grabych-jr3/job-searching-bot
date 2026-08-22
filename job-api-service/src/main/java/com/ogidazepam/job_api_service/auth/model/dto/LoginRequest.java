package com.ogidazepam.job_api_service.auth.model.dto;

public record LoginRequest(
        String email,
        String password
) {
}
