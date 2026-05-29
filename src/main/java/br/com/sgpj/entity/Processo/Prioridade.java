package br.com.sgpj.entity.Processo;

import jakarta.persistence.*;

@Entity
@Table(name = "prioridade")
public class Prioridade {

    @Id
    private Long id;
    private String nome;

    protected Prioridade() {
    }

    public Prioridade(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

}
