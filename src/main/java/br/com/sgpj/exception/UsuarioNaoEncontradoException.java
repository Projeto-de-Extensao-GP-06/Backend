package br.com.sgpj.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(String email) {
        super("Usuário não encontrado");
    }

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário não encontrado");
    }
}

