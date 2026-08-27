package com.ogidazepam.search_service.client;

import com.ogidazepam.search_service.exception.*;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class JobHttpClient {

    private static final String NEXT_DATA_SCRIPT_ID = "__NEXT_DATA__";
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY = 1000;

    private final ObjectMapper objectMapper;

    public JobHttpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode extractNextDataWithRetry(String url){
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return extractNextData(url);
            } catch (HttpStatusException e) {
                int status = e.getStatusCode();

                if (status == 404 || status == 410){
                    throw new OfferNotFoundException("Resource not found at: " + url);
                }

                if (status == 403){
                    throw new ScraperBlockedException("Scraper blocked with 403 Forbidden at " + url, e);
                }

                if (status == 429) {
                    if (attempt == MAX_ATTEMPTS){
                        throw new ScraperRateLimitException("HTTP error " + e.getStatusCode() + " fetching " + url, e);
                    }
                    backoff(attempt);
                    continue;
                }

                if (status >= 500) {
                    if (attempt == MAX_ATTEMPTS) {
                        throw new ScraperUnavailableException("Target server error (HTTP " + status + ") for " + url, e);
                    }
                    backoff(attempt);
                    continue;
                }

                throw new ScraperUnavailableException("Unexpected HTTP error " + status + " for " + url, e);
            } catch (SocketTimeoutException | ConnectException e){
                if (attempt == MAX_ATTEMPTS){
                    throw new ScraperUnavailableException("Network connection failed for " + url + " after " +
                            MAX_ATTEMPTS + " retries", e);
                }
                backoff(attempt);
            } catch (IOException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new ScraperParsingException("Failed to read/parse response from " + url, e);
                }
                backoff(attempt);
            }
        }

        throw new ScraperUnavailableException("Retry attempts exhausted unexpectedly for " + url, null);
    }

    private JsonNode extractNextData(String url) throws IOException {
        Document document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Sec-Ch-Ua", "\"Not-A.Brand\";v=\"99\", \"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\"")
                .header("Sec-Ch-Ua-Mobile", "?0")
                .header("Sec-Ch-Ua-Platform", "\"Windows\"")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Sec-Fetch-User", "?1")
                .header("Upgrade-Insecure-Requests", "1")
                .referrer("https://www.pracuj.pl/")
                .timeout(10000)
                .get();
        Element element = document.getElementById(NEXT_DATA_SCRIPT_ID);

        if (element == null) {
            throw new ScraperParsingException("__NEXT_DATA__ script tag missing from HTML at " + url);
        }

        return objectMapper.readValue(element.data(), JsonNode.class);
    }

    private void backoff(int attempt){
        try {
            long jitter = ThreadLocalRandom.current().nextLong(100, 400);
            long delay = (INITIAL_DELAY * (1L << (attempt - 1))) + jitter;
            Thread.sleep(delay);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new ScraperUnavailableException("Thread interrupted during backoff sleep", e);
        }
    }
}
