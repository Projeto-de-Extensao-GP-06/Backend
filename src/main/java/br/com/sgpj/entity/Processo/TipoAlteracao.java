package br.com.sgpj.entity.Processo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_alteracao")
public class TipoAlteracao {

    @Id
    private Long id;
    private String nome;

    public TipoAlteracao() {
    }

    public TipoAlteracao(Long id, String nome) {
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
