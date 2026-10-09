package br.com.sgpj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuração de validação de segredos e credenciais da aplicação.
 *
 * <p>Esta classe valida que todas as variáveis de ambiente críticas estejam configuradas
 * antes de permitir que a aplicação inicie, prevenindo a exposição de segredos padrão
 * ou valores vazios em produção.</p>
 *
 * <p><b>Segredos validados:</b></p>
 * <ul>
 *   <li>{@code DB_PASSWORD}: Senha do banco de dados</li>
 *   <li>{@code JWT_SECRET}: Chave secreta para assinatura de tokens JWT</li>
 *   <li>{@code CLIENT_ID}: ID do cliente Microsoft Graph API</li>
 *   <li>{@code CLIENT_SECRET}: Secret do cliente Microsoft Graph API</li>
 *   <li>{@code DATAJUD_API_KEY}: Chave de API para DataJud</li>
 * </ul>
 *
 * <p><b>Controle de validação:</b></p>
 * <p>A validação pode ser desabilitada em desenvolvimento configurando
 * {@code security.validate-secrets=false} em application.properties.</p>
 *
 * <p><b>Propósito de segurança:</b></p>
 * <p>Previne a execução da aplicação com segredos hardcoded ou ausentes,
 * evitando vulnerabilidades OWASP A02:2021 (Cryptographic Failures).</p>
 */
@Configuration
public class SecretValidationConfig {

    @Value("${security.validate-secrets:true}")
    private boolean validateSecretsEnabled;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Value("${JWT_SECRET:}")
    private String jwtSecret;

    @Value("${CLIENT_ID:}")
    private String clientId;

    @Value("${CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${DATAJUD_API_KEY:}")
    private String dataJudApiKey;

    @Value("${CLOUDFLARE_SECRET_KEY:}")
    private String cloudflareSecretKey;

    /**
     * Valida que todos os segredos críticos estejam configurados.
     *
     * <p>Este método é executado automaticamente pelo Spring após a injeção de dependências.
     * Se algum segredo estiver ausente ou vazio, a aplicação não iniciará.</p>
     *
     * @throws IllegalStateException se algum segredo crítico não estiver configurado
     */
    @PostConstruct
    public void validateSecrets() {
        if (!validateSecretsEnabled) {
            return;
        }
        
        validateRequiredSecret("DB_PASSWORD", dbPassword);
        validateRequiredSecret("JWT_SECRET", jwtSecret);
        validateRequiredSecret("CLIENT_ID", clientId);
        validateRequiredSecret("CLIENT_SECRET", clientSecret);
        validateRequiredSecret("DATAJUD_API_KEY", dataJudApiKey);
        validateRequiredSecret("CLOUDFLARE_SECRET_KEY", cloudflareSecretKey);
    }

    /**
     * Valida que um segredo específico não está vazio ou nulo.
     *
     * @param secretName nome da variável de ambiente/propriedade
     * @param secretValue valor do segredo a ser validado
     * @throws IllegalStateException se o segredo estiver ausente ou vazio
     */
    private void validateRequiredSecret(String secretName, String secretValue) {
        if (secretValue == null || secretValue.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Critical security configuration missing: " + secretName + 
                    " environment variable must be set. Application cannot start without this secret."
            );
        }
    }

    // Setters for testing purposes
    public void setValidateSecretsEnabled(boolean validateSecretsEnabled) {
        this.validateSecretsEnabled = validateSecretsEnabled;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setDataJudApiKey(String dataJudApiKey) {
        this.dataJudApiKey = dataJudApiKey;
    }
}
