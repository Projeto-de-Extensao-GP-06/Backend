package br.com.sgpj.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {

    public UsuarioNaoEncontradoException(String email) {
        super("Usuário não encontrado com o e-mail: " + email);
    }
}

