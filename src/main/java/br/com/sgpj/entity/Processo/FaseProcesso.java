package br.com.sgpj.entity.Processo;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FaseProcesso {

    @Id
    private Long id;
    private String nome;

    protected FaseProcesso() {
    }

    public FaseProcesso(Long id, String nome) {
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
