package br.com.sgpj.exception;

/**
 * Exception thrown when a rate limit is exceeded for public endpoints.
 * This prevents abuse of OTP endpoints and form submissions.
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}