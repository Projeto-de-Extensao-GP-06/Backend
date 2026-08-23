package br.com.sgpj.exception;

/**
 * Exception thrown when OTP generation rate limit is exceeded.
 * This prevents abuse of OTP generation functionality.
 */
public class OtpGenerationLimitExceededException extends RuntimeException {
    public OtpGenerationLimitExceededException(String message) {
        super(message);
    }
}