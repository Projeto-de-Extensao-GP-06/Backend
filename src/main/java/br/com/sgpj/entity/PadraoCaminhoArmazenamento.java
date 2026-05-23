package br.com.sgpj.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "padrao_caminho_armazenamento")
public class PadraoCaminhoArmazenamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nome;

    @Column(name = "caminho_base", nullable = false, length = 255, unique = true)
    private String caminhoBase;

    public PadraoCaminhoArmazenamento() {}

    public PadraoCaminhoArmazenamento(String nome, String caminhoBase) {
        this.nome = nome;
        this.caminhoBase = caminhoBase;
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

    public String getCaminhoBase() {
        return caminhoBase;
    }

    public void setCaminhoBase(String caminhoBase) {
        this.caminhoBase = caminhoBase;
    }
}

