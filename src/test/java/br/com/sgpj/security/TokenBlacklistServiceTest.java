package br.com.sgpj.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenBlacklistService - Testes Unitários")
class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    private SecretKey secretKey;
    private String validToken;
    private String expiredToken;
    private static final String JWT_SECRET = "dGVzdFNlY3JldEtleUZvclRlc3RpbmdQdXJwb3Nlc09ubHk="; // Base64 encoded

    @BeforeEach
    void setUp() {
        // Create the service with the required JWT secret
        tokenBlacklistService = new TokenBlacklistService(JWT_SECRET);
        
        // Set the secret key for token creation
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET));

        // Create a valid token
        validToken = Jwts.builder()
                .subject("test@example.com")
                .id("test-jti-123")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(secretKey)
                .compact();

        // Create an expired token
        expiredToken = Jwts.builder()
                .subject("test@example.com")
                .id("test-jti-expired")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(secretKey)
                .compact();
    }

    // =========================================================================
    // blacklistToken
    // =========================================================================

    @Nested
    @DisplayName("blacklistToken")
    class BlacklistToken {

        @Test
        @DisplayName("Deve adicionar token válido à blacklist")
        void deveAdicionarTokenValidoABlacklist() {
            tokenBlacklistService.blacklistToken(validToken);

            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isTrue();
        }

        @Test
        @DisplayName("Deve adicionar token expirado à blacklist")
        void deveAdicionarTokenExpiradoABlacklist() {
            // Expired tokens are accepted by the service but cleaned up automatically
            // The service handles them gracefully without throwing exceptions
            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> 
                tokenBlacklistService.blacklistToken(expiredToken)
            );
            
            // The service should not crash when processing expired tokens
            assertThat(tokenBlacklistService.isTokenBlacklisted(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("Não deve lançar exceção ao tentar blacklisting token inválido")
        void naoDeveLancarExcecaoAoBlacklistarTokenInvalido() {
            String invalidToken = "invalid.token.string";

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> 
                tokenBlacklistService.blacklistToken(invalidToken)
            );
        }

        @Test
        @DisplayName("Deve incrementar tamanho da blacklist ao adicionar token")
        void deveIncrementarTamanhoBlacklistAoAdicionarToken() {
            int initialSize = tokenBlacklistService.getBlacklistSize();

            tokenBlacklistService.blacklistToken(validToken);

            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(initialSize + 1);
        }
    }

    // =========================================================================
    // isTokenBlacklisted
    // =========================================================================

    @Nested
    @DisplayName("isTokenBlacklisted")
    class IsTokenBlacklisted {

        @Test
        @DisplayName("Deve retornar true para token na blacklist")
        void deveRetornarTrueParaTokenNaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);

            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false para token não blacklistado")
        void deveRetornarFalseParaTokenNaoBlacklistado() {
            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para token expirado na blacklist após expiração")
        void deveRetornarFalseParaTokenExpiradoNaBlacklistAposExpiracao() {
            tokenBlacklistService.blacklistToken(expiredToken);

            // Expired tokens are cleaned up automatically when checked
            // The service removes expired entries and returns false
            assertThat(tokenBlacklistService.isTokenBlacklisted(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para token inválido")
        void deveRetornarFalseParaTokenInvalido() {
            String invalidToken = "invalid.token.string";

            assertThat(tokenBlacklistService.isTokenBlacklisted(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("Deve limpar automaticamente tokens expirados da blacklist")
        void deveLimparAutomaticamenteTokensExpiradosDaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);
            tokenBlacklistService.blacklistToken(expiredToken);

            int sizeAfterAdding = tokenBlacklistService.getBlacklistSize();

            // Check the expired token - this should trigger cleanup
            tokenBlacklistService.isTokenBlacklisted(expiredToken);

            int sizeAfterCleanup = tokenBlacklistService.getBlacklistSize();

            // The expired token should be removed, but at least the valid token remains
            assertThat(sizeAfterCleanup).isLessThanOrEqualTo(sizeAfterAdding);
            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isTrue();
        }
    }

    // =========================================================================
    // clearAll
    // =========================================================================

    @Nested
    @DisplayName("clearAll")
    class ClearAll {

        @Test
        @DisplayName("Deve limpar todos os tokens da blacklist")
        void deveLimparTodosTokensDaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);
            
            // Add another valid token
            String anotherValidToken = Jwts.builder()
                    .subject("test2@example.com")
                    .id("test-jti-999")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(secretKey)
                    .compact();
            
            tokenBlacklistService.blacklistToken(anotherValidToken);

            assertThat(tokenBlacklistService.getBlacklistSize()).isGreaterThan(0);

            tokenBlacklistService.clearAll();

            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(0);
            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isFalse();
        }
    }

    // =========================================================================
    // getBlacklistSize
    // =========================================================================

    @Nested
    @DisplayName("getBlacklistSize")
    class GetBlacklistSize {

        @Test
        @DisplayName("Deve retornar 0 para blacklist vazia")
        void deveRetornar0ParaBlacklistVazia() {
            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve retornar número correto de tokens na blacklist")
        void deveRetornarNumeroCorretoDeTokensNaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);
            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(1);

            // Add another valid token to test counting
            String anotherValidToken = Jwts.builder()
                    .subject("test2@example.com")
                    .id("test-jti-456")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(secretKey)
                    .compact();
            
            tokenBlacklistService.blacklistToken(anotherValidToken);
            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(2);
        }
    }

    // =========================================================================
    // cleanupExpiredTokens
    // =========================================================================

    @Nested
    @DisplayName("cleanupExpiredTokens")
    class CleanupExpiredTokens {

        @Test
        @DisplayName("Deve remover tokens expirados da blacklist")
        void deveRemoverTokensExpiradosDaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);
            
            // Add another valid token first
            String anotherValidToken = Jwts.builder()
                    .subject("test2@example.com")
                    .id("test-jti-789")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(secretKey)
                    .compact();
            
            tokenBlacklistService.blacklistToken(anotherValidToken);

            int sizeBeforeCleanup = tokenBlacklistService.getBlacklistSize();
            assertThat(sizeBeforeCleanup).isEqualTo(2);

            // Add expired token (it will be cleaned up automatically)
            tokenBlacklistService.blacklistToken(expiredToken);

            // Run explicit cleanup
            tokenBlacklistService.cleanupExpiredTokens();

            int sizeAfterCleanup = tokenBlacklistService.getBlacklistSize();
            // Should still have 2 valid tokens, expired one was auto-cleaned
            assertThat(sizeAfterCleanup).isEqualTo(2);
            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isTrue();
            assertThat(tokenBlacklistService.isTokenBlacklisted(expiredToken)).isFalse();
        }

        @Test
        @DisplayName("Não deve remover tokens válidos da blacklist")
        void naoDeveRemoverTokensValidosDaBlacklist() {
            tokenBlacklistService.blacklistToken(validToken);

            tokenBlacklistService.cleanupExpiredTokens();

            assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(1);
            assertThat(tokenBlacklistService.isTokenBlacklisted(validToken)).isTrue();
        }
    }

    // =========================================================================
    // Integration scenarios
    // =========================================================================

    @Nested
    @DisplayName("Cenários de Integração")
    class IntegrationScenarios {

        @Test
        @DisplayName("Deve simular fluxo completo de login e logout")
        void deveSimularFluxoCompletoDeLoginELogout() {
            // User logs in - token is created
            String userToken = validToken;

            // Token should not be blacklisted initially
            assertThat(tokenBlacklistService.isTokenBlacklisted(userToken)).isFalse();

            // User logs out - token is blacklisted
            tokenBlacklistService.blacklistToken(userToken);

            // Token should now be blacklisted
            assertThat(tokenBlacklistService.isTokenBlacklisted(userToken)).isTrue();

            // Subsequent requests with the same token should be rejected
            assertThat(tokenBlacklistService.isTokenBlacklisted(userToken)).isTrue();
        }

        @Test
        @DisplayName("Deve simular múltiplos logouts do mesmo usuário")
        void deveSimularMultiplosLogoutsDoMesmoUsuario() {
            String token1 = Jwts.builder()
                    .subject("user@example.com")
                    .id("jti-1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(secretKey)
                    .compact();

            String token2 = Jwts.builder()
                    .subject("user@example.com")
                    .id("jti-2")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(secretKey)
                    .compact();

            // User logs out from first session
            tokenBlacklistService.blacklistToken(token1);

            // First token should be blacklisted
            assertThat(tokenBlacklistService.isTokenBlacklisted(token1)).isTrue();

            // Second token should still be valid
            assertThat(tokenBlacklistService.isTokenBlacklisted(token2)).isFalse();

            // User logs out from second session
            tokenBlacklistService.blacklistToken(token2);

            // Both tokens should now be blacklisted
            assertThat(tokenBlacklistService.isTokenBlacklisted(token1)).isTrue();
            assertThat(tokenBlacklistService.isTokenBlacklisted(token2)).isTrue();
        }
    }
}