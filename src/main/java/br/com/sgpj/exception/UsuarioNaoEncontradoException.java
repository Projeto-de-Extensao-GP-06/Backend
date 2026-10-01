package br.com.sgpj.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado");
    }

    public UsuarioNaoEncontradoException(String email) {
        super("Usuário não encontrado com o e-mail: " + email);
    }

    public UsuarioNaoEncontradoException(Long id) {
        super("Usuário não encontrado com ID " + id);
    }
}

