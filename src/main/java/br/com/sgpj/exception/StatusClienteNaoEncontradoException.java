package br.com.sgpj.exception;

public class StatusClienteNaoEncontradoException extends RuntimeException {

    public StatusClienteNaoEncontradoException(Long id) {
        super("Status do cliente não encontrado para o ID: " + id);
    }

    public StatusClienteNaoEncontradoException(String nome) {
        super("Status do cliente não encontrado: " + nome);
    }
}
