package br.com.sgpj.entity;

public enum ComoConheceuEnum {
    INSTAGRAM("Instagram"),
    SITE("Site Institucional"),
    INDICACAO("Indicação"),
    OUTROS("Outros");

    private final String descricao;

    ComoConheceuEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
