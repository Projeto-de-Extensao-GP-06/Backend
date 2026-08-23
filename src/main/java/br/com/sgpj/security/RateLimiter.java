package br.com.sgpj.security;

import br.com.sgpj.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Rate limiter to prevent abuse of public endpoints.
 * Uses a sliding window approach to track request rates per identifier (IP or email).
 */
@Component
public class RateLimiter {

    private final ConcurrentMap<String, RequestHistory> requestHistory = new ConcurrentHashMap<>();
    
    // Rate limit configuration: max requests per time window
    private static final int MAX_REQUESTS_PER_HOUR = 10;
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 3;
    private static final int MAX_LOGIN_REQUESTS_PER_MINUTE = 3;
    private static final int MAX_LOGIN_REQUESTS_PER_HOUR = 5;
    
    /**
     * Checks if the identifier has exceeded the rate limit for general public endpoints.
     * 
     * @param identifier Unique identifier (email, IP address, etc.)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkRateLimit(String identifier) {
        checkRateLimit(identifier, MAX_REQUESTS_PER_HOUR, ChronoUnit.HOURS);
        checkRateLimit(identifier, MAX_REQUESTS_PER_MINUTE, ChronoUnit.MINUTES);
    }
    
    /**
     * Checks if the identifier has exceeded the rate limit for OTP-specific endpoints.
     * OTP endpoints have stricter limits to prevent abuse.
     * 
     * @param identifier Unique identifier (email, IP address, etc.)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkOtpRateLimit(String identifier) {
        checkRateLimit(identifier, MAX_OTP_REQUESTS_PER_HOUR, ChronoUnit.HOURS);
        checkRateLimit(identifier, MAX_REQUESTS_PER_MINUTE, ChronoUnit.MINUTES);
    }
    
    /**
     * Checks if the identifier has exceeded the rate limit for form submission endpoints.
     * 
     * @param identifier Unique identifier (email, IP address, etc.)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkFormRateLimit(String identifier) {
        checkRateLimit(identifier, MAX_REQUESTS_PER_HOUR, ChronoUnit.HOURS);
        checkRateLimit(identifier, MAX_REQUESTS_PER_MINUTE, ChronoUnit.MINUTES);
    }
    
    /**
     * Checks if the identifier has exceeded the rate limit for login endpoints.
     * Login endpoints have stricter limits to prevent brute force attacks.
     * 
     * @param identifier Unique identifier (email, IP address, etc.)
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    public void checkLoginRateLimit(String identifier) {
        checkRateLimit(identifier, MAX_LOGIN_REQUESTS_PER_HOUR, ChronoUnit.HOURS);
        checkRateLimit(identifier, MAX_LOGIN_REQUESTS_PER_MINUTE, ChronoUnit.MINUTES);
    }
    
    /**
     * Core rate limiting logic using sliding window.
     * 
     * @param identifier Unique identifier
     * @param maxRequests Maximum allowed requests in the time window
     * @param timeUnit Time unit for the window
     * @throws RateLimitExceededException if rate limit is exceeded
     */
    private void checkRateLimit(String identifier, int maxRequests, ChronoUnit timeUnit) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be null or blank");
        }
        
        String key = identifier + ":" + timeUnit.name();
        RequestHistory history = requestHistory.computeIfAbsent(key, k -> new RequestHistory());
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(1, timeUnit);
        
        // Clean up old requests outside the time window and count current requests
        int currentCount = history.cleanupAndCount(windowStart);
        
        // Check if rate limit is exceeded
        if (currentCount >= maxRequests) {
            throw new RateLimitExceededException(
                String.format("Rate limit exceeded: maximum %d requests per %s allowed", 
                maxRequests, timeUnit.name().toLowerCase())
            );
        }
        
        // Record this request
        history.addRequest(now);
    }
    
    /**
     * Inner class to track request history for a specific identifier and time unit.
     */
    private static class RequestHistory {
        private final CopyOnWriteArrayList<LocalDateTime> requests = new CopyOnWriteArrayList<>();
        
        public void addRequest(LocalDateTime timestamp) {
            requests.add(timestamp);
        }
        
        public int cleanupAndCount(LocalDateTime windowStart) {
            requests.removeIf(timestamp -> timestamp.isBefore(windowStart));
            return requests.size();
        }
    }
    
    /**
     * Clears all rate limit history. Useful for testing.
     */
    public void clearAll() {
        requestHistory.clear();
    }
    
    /**
     * Clears rate limit history for a specific identifier.
     * 
     * @param identifier The identifier to clear
     */
    public void clearForIdentifier(String identifier) {
        requestHistory.keySet().removeIf(key -> key.startsWith(identifier + ":"));
    }
}