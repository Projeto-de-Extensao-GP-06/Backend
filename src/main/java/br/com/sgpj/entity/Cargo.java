package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cargo")
public class Cargo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 90)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    @Column(nullable = false)
    private LocalDate criadoEm;

    @OneToMany(mappedBy = "cargo", fetch = FetchType.EAGER)
    private List<CargoPermissao> permissoes;

    // Constructors
    public Cargo() {}

    public Cargo(Long id, String nome, Usuario criadoPor, LocalDate criadoEm) {
        this.id = id;
        this.nome = nome;
        this.criadoPor = criadoPor;
        this.criadoEm = criadoEm;
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

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<CargoPermissao> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(List<CargoPermissao> permissoes) {
        this.permissoes = permissoes;
    }
}
