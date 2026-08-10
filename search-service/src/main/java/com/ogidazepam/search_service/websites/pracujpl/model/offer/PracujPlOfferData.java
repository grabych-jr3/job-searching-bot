package com.ogidazepam.search_service.websites.pracujpl.model.offer;

import java.util.List;

public record PracujPlOfferData(
        PracujPlOfferPublicationDetails publicationDetails,
        PracujPlOfferAttributes attributes,
        List<PracujPlOfferTextSection> textSections
) {
}
