package com.ogidazepam.search_service.pracujpl.model.offer;

import java.util.List;

public record PracujPlOfferAttributes(
        String jobTitle,
        String description,
        String offerAbsoluteUrl,
        String displayEmployerName,
        List<PracujPlOfferWorkplace> workplaces,
        PracujPlOfferEmployment employment
) {
}
