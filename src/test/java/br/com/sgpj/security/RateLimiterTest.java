package br.com.sgpj.security;

import br.com.sgpj.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

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
            
            // Should allow up to MAX_REQUESTS_PER_MINUTE requests
            for (int i = 0; i < RateLimiter.MAX_REQUESTS_PER_MINUTE; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkRateLimit(identifier));
            }
        }

        @Test
        @DisplayName("Deve lançar exceção quando limite por minuto é excedido")
        void deveLancarExcecaoQuandoLimiteMinutoExcedido() {
            String identifier = "test-user@example.com";
            
            // Make max requests per minute
            for (int i = 0; i < RateLimiter.MAX_REQUESTS_PER_MINUTE; i++) {
                rateLimiter.checkRateLimit(identifier);
            }
            
            // Next request should exceed limit
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
        @DisplayName("Deve aplicar limites por minuto para OTP")
        void deveAplicarLimitesPorMinutoParaOtp() {
            String identifier = "otp-minute@example.com";
            
            for (int i = 0; i < RateLimiter.MAX_OTP_REQUESTS_PER_MINUTE; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkOtpRateLimit(identifier));
            }
            
            // Next request should exceed minute limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkOtpRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve aplicar limites por hora para OTP")
        void deveAplicarLimitesPorHoraParaOtp() {
            String identifier = "otp-hourly@example.com";
            
            // Clear minute window to test hourly limit accumulation
            for (int i = 0; i < RateLimiter.MAX_OTP_REQUESTS_PER_HOUR; i++) {
                rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
                assertDoesNotThrow(() -> rateLimiter.checkOtpRateLimit(identifier));
            }
            
            // Next request should exceed hourly limit
            rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkOtpRateLimit(identifier));
        }
    }

    @Nested
    @DisplayName("checkFormRateLimit - Testes de rate limit formulário")
    class CheckFormRateLimitTest {

        @Test
        @DisplayName("Deve aplicar limites padrão por minuto para formulários")
        void deveAplicarLimitesPadraoParaFormularios() {
            String identifier = "form-user@example.com";
            
            // Forms use dedicated form limits
            for (int i = 0; i < RateLimiter.MAX_FORM_REQUESTS_PER_MINUTE; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkFormRateLimit(identifier));
            }
            
            // Next request should exceed limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkFormRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve aplicar limites por hora para formulários")
        void deveAplicarLimitesPorHoraParaFormularios() {
            String identifier = "form-hourly@example.com";
            
            for (int i = 0; i < RateLimiter.MAX_FORM_REQUESTS_PER_HOUR; i++) {
                rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
                assertDoesNotThrow(() -> rateLimiter.checkFormRateLimit(identifier));
            }
            
            rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkFormRateLimit(identifier));
        }
    }

    @Nested
    @DisplayName("checkLoginRateLimit - Testes de rate limit login")
    class CheckLoginRateLimitTest {

        @Test
        @DisplayName("Deve aplicar limites mais estritos para login por minuto")
        void deveAplicarLimitesMaisEstritosParaLogin() {
            String identifier = "login-user@example.com";
            
            // Login has limit: MAX_LOGIN_REQUESTS_PER_MINUTE
            for (int i = 0; i < RateLimiter.MAX_LOGIN_REQUESTS_PER_MINUTE; i++) {
                assertDoesNotThrow(() -> rateLimiter.checkLoginRateLimit(identifier));
            }
            
            // Next request should exceed login limit
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkLoginRateLimit(identifier));
        }

        @Test
        @DisplayName("Deve aplicar limites para login por hora")
        void deveAplicarLimitesParaLoginPorHora() {
            String identifier = "login-hourly@example.com";
            
            for (int i = 0; i < RateLimiter.MAX_LOGIN_REQUESTS_PER_HOUR; i++) {
                rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
                assertDoesNotThrow(() -> rateLimiter.checkLoginRateLimit(identifier));
            }
            
            rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
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
        @DisplayName("Deve limpar histórico para identificador e unidade de tempo específica")
        void deveLimparHistoricoParaIdentificadorEUnidadeTempo() {
            String identifier = "cleanup-unit-test@example.com";
            
            for (int i = 0; i < RateLimiter.MAX_REQUESTS_PER_MINUTE; i++) {
                rateLimiter.checkRateLimit(identifier);
            }
            
            // Minute limit exceeded
            assertThrows(RateLimitExceededException.class, () -> rateLimiter.checkRateLimit(identifier));
            
            // Clear only minute window
            rateLimiter.clearForIdentifier(identifier, ChronoUnit.MINUTES);
            
            // Can make request again
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
            for (int i = 0; i < RateLimiter.MAX_REQUESTS_PER_MINUTE; i++) {
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
            for (int i = 0; i < RateLimiter.MAX_REQUESTS_PER_MINUTE; i++) {
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