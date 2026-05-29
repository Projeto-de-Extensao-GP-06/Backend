package br.com.sgpj.exception;

public class CaminhoJaExisteException extends RuntimeException {
    public CaminhoJaExisteException(String caminho) {
        super("Já existe um caminho de armazenamento cadastrado: " + caminho);
    }
}

