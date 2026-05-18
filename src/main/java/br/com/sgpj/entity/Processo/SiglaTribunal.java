package br.com.sgpj.entity.Processo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sigla_tribunal")
public class SiglaTribunal {

    @Id
    private Long id;
    private String nome;

    protected SiglaTribunal() {
    }

    public SiglaTribunal(Long id, String nome) {
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
