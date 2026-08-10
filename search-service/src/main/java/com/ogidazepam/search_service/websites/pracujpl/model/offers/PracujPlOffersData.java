package com.ogidazepam.search_service.websites.pracujpl.model.offers;

import java.util.List;

public record PracujPlOffersData(
        List<PracujPlOffersGroupedOffer> groupedOffers
) {
}
