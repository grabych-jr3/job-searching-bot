package com.ogidazepam.search_service.exception;

public class ScraperParsingException extends ScraperException {
    public ScraperParsingException(String message) {
        super(message);
    }
    public ScraperParsingException(String message, Throwable cause) { super(message, cause); }
}
