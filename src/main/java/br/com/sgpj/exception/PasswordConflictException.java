package br.com.sgpj.exception;

public class PasswordConflictException extends RuntimeException {
    public PasswordConflictException(String message) {
        super(message);
    }
}
