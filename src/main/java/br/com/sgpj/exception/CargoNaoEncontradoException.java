package br.com.sgpj.exception;

public class CargoNaoEncontradoException extends RuntimeException {
    public CargoNaoEncontradoException(String message) {
        super(message);
    }
}
