package br.com.sgpj.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing JWT token blacklist/revocation.
 * 
 * <p>This service provides server-side token invalidation capability,
 * addressing the security vulnerability where stolen JWT tokens remain
 * valid until their natural expiration.</p>
 * 
 * <p><b>Security benefits:</b></p>
 * <ul>
 *   <li>Immediate token invalidation on logout</li>
 *   <li>Token revocation for compromised sessions</li>
 *   <li>Memory-efficient cleanup of expired entries</li>
 * </ul>
 */
@Service
public class TokenBlacklistService {

    private final ConcurrentMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    private final SecretKey secretKey;

    public TokenBlacklistService(@Value("${jwt.secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
    }

    /**
     * Adds a token to the blacklist.
     * 
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String tokenKey = extractTokenKey(token);
            LocalDateTime expiration = extractExpiration(token);
            blacklistedTokens.put(tokenKey, expiration);
        } catch (Exception e) {
            // If token is invalid, we don't need to blacklist it
            // This prevents errors when trying to blacklist malformed tokens
        }
    }

    /**
     * Checks if a token is blacklisted.
     * 
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenKey = extractTokenKey(token);
            LocalDateTime expiration = blacklistedTokens.get(tokenKey);
            
            if (expiration == null) {
                return false;
            }
            
            // Clean up expired entries and return false if expired
            if (expiration.isBefore(LocalDateTime.now())) {
                blacklistedTokens.remove(tokenKey);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            // If token is invalid, treat it as not blacklisted
            // The JWT validation will fail elsewhere
            return false;
        }
    }

    /**
     * Extracts a unique key for the token from its JWT ID (jti) claim.
     * If jti is not present, uses the token signature as the key.
     * 
     * @param token JWT token
     * @return unique key for the token
     */
    private String extractTokenKey(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Prefer using jti (JWT ID) if available
            String jti = claims.getId();
            if (jti != null && !jti.isEmpty()) {
                return jti;
            }
            
            // Fallback to using a combination of subject and expiration
            return claims.getSubject() + ":" + claims.getExpiration().getTime();
        } catch (Exception e) {
            // Fallback to simple hash of the token if parsing fails
            return String.valueOf(token.hashCode());
        }
    }

    /**
     * Extracts the expiration time from the token.
     * 
     * @param token JWT token
     * @return expiration time as LocalDateTime
     */
    private LocalDateTime extractExpiration(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        Date expiration = claims.getExpiration();
        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Removes expired tokens from the blacklist.
     * Runs every 30 minutes to prevent memory leaks.
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> 
            entry.getValue().isBefore(now)
        );
    }

    /**
     * Clears all blacklisted tokens.
     * Useful for testing or emergency situations.
     */
    public void clearAll() {
        blacklistedTokens.clear();
    }

    /**
     * Gets the current size of the blacklist.
     * 
     * @return number of blacklisted tokens
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}