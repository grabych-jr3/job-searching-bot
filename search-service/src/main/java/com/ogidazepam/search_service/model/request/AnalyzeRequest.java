package com.ogidazepam.search_service.model.request;

import java.util.List;

public record AnalyzeRequest(
        String technology,
        List<String> experience,
        List<String> workMode
) {
}