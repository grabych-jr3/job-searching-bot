package com.ogidazepam.search_service.websites.pracujpl.model.offer;

import java.util.List;

public record PracujPlOfferTextSection(
        String sectionType,
        List<String> textElements
) {
}
