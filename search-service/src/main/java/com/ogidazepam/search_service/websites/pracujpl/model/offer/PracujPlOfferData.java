package com.ogidazepam.search_service.websites.pracujpl.model.offer;

import java.util.List;

public record PracujPlOfferData(
        PracujPlOfferAttributes attributes,
        List<PracujPlOfferTextSection> textSections
) {
}
