package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "permissao")
public class Permissao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @OneToMany(mappedBy = "permissao", fetch = FetchType.EAGER)
    private List<CargoPermissao> cargos;

    // Constructors
    public Permissao() {}

    public Permissao(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters and Setters
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

    public List<CargoPermissao> getCargos() {
        return cargos;
    }

    public void setCargos(List<CargoPermissao> cargos) {
        this.cargos = cargos;
    }
}
