package com.ogidazepam.job_api_service.model.response;

import com.ogidazepam.job_api_service.model.OfferResult;

public record SseResponseOfferResult(
        OfferResult offerResult,
        Boolean isNew
) {
}
