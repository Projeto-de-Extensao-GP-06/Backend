package br.com.sgpj.exception;

/**
 * Exceção lançada quando há falha na integração com a API DataJud.
 * Pode ser falha de conexão, timeout ou erro na resposta da API.
 */
public class DataJudIntegrationException extends RuntimeException {

    private final String endpoint;
    private final int statusCode;

    /**
     * Exceção com mensagem simples (ex: timeout, conexão recusada).
     */
    public DataJudIntegrationException(String message) {
        this(message, null, -1);
    }

    /**
     * Exceção com detalhes técnicos completos.
     */
    public DataJudIntegrationException(String message, String endpoint, int statusCode) {
        super("Falha na integração com DataJud: " + message);
        this.endpoint = endpoint;
        this.statusCode = statusCode;
    }

    /**
     * Exceção com causa raiz.
     */
    public DataJudIntegrationException(String message, Throwable cause) {
        super("Falha na integração com DataJud: " + message, cause);
        this.endpoint = null;
        this.statusCode = -1;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isServerError() {
        return statusCode >= 500;
    }

    public boolean isClientError() {
        return statusCode >= 400 && statusCode < 500;
    }

    public boolean isConnectionError() {
        return statusCode == -1;
    }
}
