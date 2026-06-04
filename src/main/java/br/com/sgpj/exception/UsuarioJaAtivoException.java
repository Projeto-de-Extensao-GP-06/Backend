package br.com.sgpj.exception;

public class UsuarioJaAtivoException extends RuntimeException {
    public UsuarioJaAtivoException(String message) {
        super(message);
    }
}
