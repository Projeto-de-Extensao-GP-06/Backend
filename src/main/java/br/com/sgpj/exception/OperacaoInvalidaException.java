package br.com.sgpj.exception;

public class OperacaoInvalidaException extends RuntimeException {
    public OperacaoInvalidaException(String message) {
        super(message);
    }
}
