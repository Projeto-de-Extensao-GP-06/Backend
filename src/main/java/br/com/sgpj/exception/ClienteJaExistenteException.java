package br.com.sgpj.exception;

public class ClienteJaExistenteException extends RuntimeException {

    public ClienteJaExistenteException(String cpfCnpj) {
        super("Já existe um cliente cadastrado com o CPF/CNPJ: " + cpfCnpj);
    }
}

