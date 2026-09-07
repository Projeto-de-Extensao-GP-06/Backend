package br.com.sgpj.entity;

public enum StatusClienteEnum {
    LEAD("LEAD"),
    ATIVO("ATIVO"),
    INATIVO("INATIVO");

    private final String nome;

    StatusClienteEnum(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
