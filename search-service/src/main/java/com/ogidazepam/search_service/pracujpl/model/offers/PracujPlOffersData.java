package com.ogidazepam.search_service.pracujpl.model.offers;

import java.util.List;

public record PracujPlOffersData(
        List<PracujPlOffersGroupedOffer> groupedOffers
) {
}
