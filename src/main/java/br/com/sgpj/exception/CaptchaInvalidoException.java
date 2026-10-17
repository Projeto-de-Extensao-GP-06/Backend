package br.com.sgpj.exception;

public class CaptchaInvalidoException extends RuntimeException {
    public CaptchaInvalidoException(String message) {
        super(message);
    }
}
