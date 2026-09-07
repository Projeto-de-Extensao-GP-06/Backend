package br.com.sgpj.service.otp;

import java.time.LocalDateTime;

public interface OtpRateLimiter {
    void checkGenerationAllowed(String email);
    void recordGeneration(String email);
    void cleanupOlderThan(LocalDateTime cutoff);
}
