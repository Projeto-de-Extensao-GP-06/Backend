package br.com.sgpj.entity.Processo;

import jakarta.persistence.*;

@Entity
@Table(name = "sigla_tribunal")
public class SiglaTribunal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sigla_tribunal", nullable = false, length = 10)
    private String nome;

    public SiglaTribunal() {
    }

    public SiglaTribunal(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
