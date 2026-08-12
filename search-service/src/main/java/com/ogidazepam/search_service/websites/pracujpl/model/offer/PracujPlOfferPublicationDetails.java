package com.ogidazepam.search_service.websites.pracujpl.model.offer;

import java.time.OffsetDateTime;

public record PracujPlOfferPublicationDetails(
        String jobOfferUrlSegment,
        OffsetDateTime expirationDateTimeUtc
) {
}
