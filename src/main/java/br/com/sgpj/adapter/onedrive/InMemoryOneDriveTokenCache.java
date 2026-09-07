package br.com.sgpj.adapter.onedrive;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class InMemoryOneDriveTokenCache implements OneDriveTokenCache {

    private volatile OneDriveTokenCache.CachedToken cached;

    @Override
    public Optional<OneDriveTokenCache.CachedToken> get() {
        return Optional.ofNullable(cached);
    }

    @Override
    public void put(OneDriveTokenCache.CachedToken token) {
        this.cached = token;
    }

    @Override
    public void invalidate() {
        this.cached = null;
    }
}
