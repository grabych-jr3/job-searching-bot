package com.ogidazepam.search_service.exception;

public class ScraperRateLimitException extends ScraperException {
    public ScraperRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
