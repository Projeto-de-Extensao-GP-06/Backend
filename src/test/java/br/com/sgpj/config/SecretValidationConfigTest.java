package br.com.sgpj.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes unitários para validação de segredos e credenciais.
 *
 * <p>Esta classe de teste valida que a configuração de segurança impede
 * a inicialização da aplicação quando segredos críticos estão ausentes,
 * seguindo as convenções de teste do projeto.</p>
 */
@DisplayName("SecretValidationConfig - Testes de Validação de Segredos")
class SecretValidationConfigTest {

    @Nested
    @DisplayName("Validação de Segredos")
    class ValidacaoSecretsTest {

        @Test
        @DisplayName("Deve lançar exceção quando DB_PASSWORD estiver vazio")
        void deveLancarExcecaoQuandoDBPasswordEstiverVazio() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("");
            config.setJwtSecret("valid-secret");
            config.setClientId("valid-id");
            config.setClientSecret("valid-secret");
            config.setDataJudApiKey("valid-key");
            config.setCloudflareSecretKey("valid-key");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve lançar exceção quando DB_PASSWORD for null")
        void deveLancarExcecaoQuandoDBPasswordForNull() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword(null);
            config.setJwtSecret("valid-secret");
            config.setClientId("valid-id");
            config.setClientSecret("valid-secret");
            config.setDataJudApiKey("valid-key");
            config.setCloudflareSecretKey("valid-key");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve lançar exceção quando JWT_SECRET estiver vazio")
        void deveLancarExcecaoQuandoJWTSecretEstiverVazio() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("valid-password");
            config.setJwtSecret("");
            config.setClientId("valid-id");
            config.setClientSecret("valid-secret");
            config.setDataJudApiKey("valid-key");
            config.setCloudflareSecretKey("valid-key");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve lançar exceção quando CLIENT_SECRET estiver vazio")
        void deveLancarExcecaoQuandoClientSecretEstiverVazio() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("valid-password");
            config.setJwtSecret("valid-secret");
            config.setClientId("valid-id");
            config.setClientSecret("");
            config.setDataJudApiKey("valid-key");
            config.setCloudflareSecretKey("valid-key");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve lançar exceção quando DATAJUD_API_KEY estiver vazio")
        void deveLancarExcecaoQuandoDataJudApiKeyEstiverVazio() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("valid-password");
            config.setJwtSecret("valid-secret");
            config.setClientId("valid-id");
            config.setClientSecret("valid-secret");
            config.setDataJudApiKey("");
            config.setCloudflareSecretKey("valid-key");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve lançar exceção quando CLOUDFLARE_SECRET_KEY estiver vazio")
        void deveLancarExcecaoQuandoCloudflareSecretKeyEstiverVazio() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("valid-password");
            config.setJwtSecret("valid-secret");
            config.setClientId("valid-id");
            config.setClientSecret("valid-secret");
            config.setDataJudApiKey("valid-key");
            config.setCloudflareSecretKey("");
            config.setValidateSecretsEnabled(true);

            assertThrows(IllegalStateException.class, config::validateSecrets);
        }

        @Test
        @DisplayName("Deve pular validação quando security.validate-secrets for false")
        void devePularValidacaoQuandoSecurityValidateSecretsForFalse() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("");
            config.setJwtSecret("");
            config.setClientId("");
            config.setClientSecret("");
            config.setDataJudApiKey("");
            config.setCloudflareSecretKey("");
            config.setValidateSecretsEnabled(false);

            assertDoesNotThrow(config::validateSecrets);
        }

        @Test
        @DisplayName("Deve passar validação quando todos os secrets estiverem definidos")
        void devePassarValidacaoQuandoTodosOsSecretsEstiveremDefinidos() {
            SecretValidationConfig config = new SecretValidationConfig();
            config.setDbPassword("valid-password");
            config.setJwtSecret("valid-jwt-secret");
            config.setClientId("valid-client-id");
            config.setClientSecret("valid-client-secret");
            config.setDataJudApiKey("valid-api-key");
            config.setCloudflareSecretKey("valid-cloudflare-key");
            config.setValidateSecretsEnabled(true);

            assertDoesNotThrow(config::validateSecrets);
        }
    }
}
