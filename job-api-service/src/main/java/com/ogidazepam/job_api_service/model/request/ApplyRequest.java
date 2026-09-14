package com.ogidazepam.job_api_service.model.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyRequest(
        @NotBlank(message = "OfferUrl is required")
        String offerUrl,

        @NotBlank(message = "JobTitle is required")
        String jobTitle,

        @NotBlank(message = "CompanyName is required")
        String companyName
) {
}
