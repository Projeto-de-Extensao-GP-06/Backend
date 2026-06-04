package br.com.sgpj.exception;

public class CargoJaExisteException extends RuntimeException {
    public CargoJaExisteException(String message) {
        super(message);
    }
}
