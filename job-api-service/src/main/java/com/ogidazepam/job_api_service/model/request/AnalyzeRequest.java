package com.ogidazepam.job_api_service.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AnalyzeRequest(
        @NotBlank(message = "Technology keyword is required")
        String technology,

        @NotEmpty(message = "Experience list cannot be empty")
        List<@NotBlank(message = "Experience element cannot be blank") String> experience,
        List<String> workMode
) {
}
