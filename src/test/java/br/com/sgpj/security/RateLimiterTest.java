package br.com.sgpj.security;

import br.com.sgpj.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimiter - Testes de proteção contra abuso")
class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Nested
    @DisplayName("checkRateLimit - Testes de rate limit geral")
    class CheckRateLimitTest {

        @Test
        @DisplayName("Deve permitir requests dentro do limite")
        void devePermitirRequestsDentroLimite() {
            String identifier = "test-user@example.com";
            
            // Should allow up to MAX_REQUESTS_PER_MINUTE (5) requests
            for (int i = 0; i < 5; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier));
            }
        }

        @Test
        @DisplayName("Deve lançar exceção quando limite por minuto é excedido")
        void deveLancarExcecaoQuandoLimiteMinutoExcedido() {
            String identifier = "test-user@example.com";
            
            // Make 5 requests (max per minute)
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit(identifier);
            }
            
            // 6th request should exceed limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve lançar exceção para identificador nulo")
        void deveLancarExcecaoParaIdentificadorNulo() {
            assertThrows(IllegalArgumentException.class, () -> rateLimiter.checkRateLimit(null));
        }

        @Test
        @DisplayName("Deve lançar exceção para identificador em branco")
        void deveLancarExcecaoParaIdentificadorEmBranco() {
            assertThrows(IllegalArgumentException.class, () -> rateLimiter.checkRateLimit(""));
            assertThrows(IllegalArgumentException.class, () -> rateLimiter.checkRateLimit("   "));
        }
    }

    @Nested
    @DisplayName("checkOtpRateLimit - Testes de rate limit OTP")
    class CheckOtpRateLimitTest {

        @Test
        @DisplayName("Deve aplicar limites mais estritos para OTP")
        void deveAplicarLimitesMaisEstritosParaOtp() {
            String identifier = "otp-user@example.com";
            
            // OTP has stricter limit: MAX_OTP_REQUESTS_PER_HOUR (3)
            for (int i = 0; i < 3; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkOtpRateLimit(identifier));
            }
            
            // 4th request should exceed OTP limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkOtpRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve respeitar limite por minuto para OTP")
        void deveRespeitarLimitePorMinutoParaOtp() {
            String identifier = "otp-minute@example.com";
            
            // Make 3 requests (max per hour for OTP)
            for (int i = 0; i < 3; i++) {
                rateLimiter.checkOtpRateLimit(identifier);
            }
            
            // Clear the hour limit but keep minute limit by using a different identifier
            String minuteOnlyIdentifier = "otp-minute-only@example.com";
            
            // Make 5 requests (max per minute)
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit(minuteOnlyIdentifier);
            }
            
            // 6th request should exceed minute limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(minuteOnlyIdentifier));
        }
    }

    @Nested
    @DisplayName("checkFormRateLimit - Testes de rate limit formulário")
    class CheckFormRateLimitTest {

        @Test
        @DisplayName("Deve aplicar limites padrão para formulários")
        void deveAplicarLimitesPadraoParaFormularios() {
            String identifier = "form-user@example.com";
            
            // Forms use standard limits
            for (int i = 0; i < 5; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkFormRateLimit(identifier));
            }
            
            // 6th request should exceed limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkFormRateLimit(identifier));
        }
    }

    @Nested
    @DisplayName("checkLoginRateLimit - Testes de rate limit login")
    class CheckLoginRateLimitTest {

        @Test
        @DisplayName("Deve aplicar limites mais estritos para login")
        void deveAplicarLimitesMaisEstritosParaLogin() {
            String identifier = "login-user@example.com";
            
            // Login has stricter limit: MAX_LOGIN_REQUESTS_PER_MINUTE (3)
            for (int i = 0; i < 3; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkLoginRateLimit(identifier));
            }
            
            // 4th request should exceed login limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkLoginRateLimit(identifier));
        }
    }

    @Nested
    @DisplayName("Gerenciamento de estado - Testes de limpeza")
    class StateManagementTest {

        @Test
        @DisplayName("Deve limpar histórico para identificador específico")
        void deveLimparHistoricoParaIdentificadorEspecifico() {
            String identifier = "cleanup-test@example.com";
            
            // Make some requests
            for (int i = 0; i < 3; i++) {
                rateLimiter.checkRateLimit(identifier);
            }
            
            // Clear for this identifier
            rateLimiter.clearForIdentifier(identifier);
            
            // Should allow requests again
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve limpar todo o histórico")
        void deveLimparTodoHistorico() {
            String identifier1 = "user1@example.com";
            String identifier2 = "user2@example.com";
            
            // Make requests for different users
            for (int i = 0; i < 3; i++) {
                rateLimiter.checkRateLimit(identifier1);
                rateLimiter.checkRateLimit(identifier2);
            }
            
            // Clear all
            rateLimiter.clearAll();
            
            // Should allow requests for both users
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier1));
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier2));
        }

        @Test
        @DisplayName("Deve isolar diferentes identificadores")
        void deveIsolarDiferentesIdentificadores() {
            String identifier1 = "user1@example.com";
            String identifier2 = "user2@example.com";
            
            // Max out requests for user1
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit(identifier1);
            }
            
            // User1 should be rate limited
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(identifier1));
            
            // User2 should still be able to make requests
            assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier2));
        }
    }

    @Nested
    @DisplayName("Mensagem de erro - Testes")
    class ErrorMessageTest {

        @Test
        @DisplayName("Deve fornecer mensagem descritiva quando limite excedido")
        void deveFornecerMensagemDescritivaQuandoLimiteExcedido() {
            String identifier = "error-test@example.com";
            
            // Max out requests
            for (int i = 0; i < 5; i++) {
                rateLimiter.checkRateLimit(identifier);
            }
            
            // Check exception message
            RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class, 
                () -> rateLimiter.checkRateLimit(identifier)
            );
            
            assertTrue(exception.getMessage().contains("Rate limit exceeded"));
            assertTrue(exception.getMessage().contains("requests per"));
        }
    }
}