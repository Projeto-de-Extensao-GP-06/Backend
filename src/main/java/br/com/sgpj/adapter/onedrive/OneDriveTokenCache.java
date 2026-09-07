package br.com.sgpj.adapter.onedrive;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Abstração para cache de token. Implementações podem ser in-memory, redis, etc.
 */
public interface OneDriveTokenCache {
    Optional<CachedToken> get();
    void put(CachedToken token);
    void invalidate();

    record CachedToken(String accessToken, LocalDateTime expiresAt) {}
}
