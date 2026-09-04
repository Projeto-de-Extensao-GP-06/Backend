package br.com.sgpj.exception;

/**
 * Exception thrown when a JWT token has been blacklisted/revoked.
 * This indicates the token was explicitly invalidated by the server.
 */
public class TokenBlacklistedException extends RuntimeException {
    public TokenBlacklistedException(String message) {
        super(message);
    }
}