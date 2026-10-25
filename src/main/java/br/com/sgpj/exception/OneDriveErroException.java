package br.com.sgpj.exception;

public class OneDriveErroException extends RuntimeException {
    public OneDriveErroException(String message) {
        super(message);
    }

    public OneDriveErroException(String message, Throwable cause) {
        super(message, cause);
    }
}
