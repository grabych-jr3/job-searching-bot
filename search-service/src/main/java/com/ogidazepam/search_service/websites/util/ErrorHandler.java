package com.ogidazepam.search_service.websites.util;

import com.ogidazepam.search_service.exception.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandler {

    public static void handleFetchError(String id, Exception e) {
        if (e instanceof OfferNotFoundException) {
            log.debug("Offer {} not found (expired/deleted), skipping", id);
        } else if (e instanceof ScraperBlockedException || e instanceof ScraperRateLimitException) {
            log.warn("Scraper blocked or rate-limited while fetching offer {}: {}", id, e.getMessage());
        } else if (e instanceof ScraperUnavailableException) {
            log.warn("The server was unavailable during fetching the offer {}: {}", id, e.getMessage());
        } else if (e instanceof ScraperParsingException) {
            log.warn("Scraper failed to parse data from {}: {}", id, e.getMessage());
        } else {
            log.error("Unexpected error parsing offer {}: {}", id, e.getMessage());
        }
    }
}
