package com.ogidazepam.job_api_service.interceptor;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import com.ogidazepam.job_api_service.exceptions.RateLimitExceededException;
import com.ogidazepam.job_api_service.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = resolveRateLimitKey(request);

        Bucket bucket = rateLimitService.resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()){
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }
        long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;

        String message = getMessage(waitForRefillSeconds);
        throw new RateLimitExceededException(message, waitForRefillSeconds);
    }

    private String resolveRateLimitKey(HttpServletRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails){
            Long id = userDetails.getCustomerId();
            return "rate_limit:customer:" + id;
        }

        return "rate_limit:ip:" + getClientIp(request);
    }

    private String getClientIp(HttpServletRequest request){
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty()){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String getMessage(long waitForRefillSeconds) {
        String message;
        if (waitForRefillSeconds > 300) {
            message = String.format("You have exhausted your daily quota (5 requests/day). Try again in %d hours.",
                    TimeUnit.SECONDS.toHours(waitForRefillSeconds));
        } else {
            message = String.format("Too many requests. Please wait %d seconds before trying again.", waitForRefillSeconds);
        }
        return message;
    }
}
