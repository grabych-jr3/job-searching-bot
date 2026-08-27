package com.ogidazepam.search_service.exception;

public class ScraperBlockedException extends ScraperException {
    public ScraperBlockedException(String message) {
        super(message);
    }

    public ScraperBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
