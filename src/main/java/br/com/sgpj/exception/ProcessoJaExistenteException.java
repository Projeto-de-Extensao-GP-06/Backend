package br.com.sgpj.exception;

public class ProcessoJaExistenteException extends RuntimeException {
    public ProcessoJaExistenteException(String message) {
        super(message);
    }
}
