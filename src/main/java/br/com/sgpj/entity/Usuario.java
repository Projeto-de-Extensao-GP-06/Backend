package br.com.sgpj.entity;


import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 250)
    private String senha;

  @ManyToOne
  @JoinColumn(name = "cargo_id")
  private Cargo cargo;

  private LocalDate criadoEm;

  private boolean ativo;

  public Usuario() {}

    @Column(nullable = false, name = "criado_em")
    private LocalDate criadoEm = LocalDate.now();

    @Column(nullable = false, name = "ativo")
    private Boolean ativo = true;

    public Usuario() {}

  public Usuario(String nome, String email, String senha) {
    this.nome = nome;
    this.email = email;
    this.senha = senha;
  }

  public Usuario(Long id, String email, String senha) {
    this.id = id;
    this.email = email;
    this.senha = senha;
  }

  public Usuario(Long id, String nome, String email, String senha, Cargo cargo, LocalDate criadoEm, boolean ativo) {
    this.id = id;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.cargo = cargo;
    this.criadoEm = criadoEm;
    this.ativo = ativo;
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

    public String getEmail() {
    return email;
    }

    public void setEmail(String email) {
    this.email = email;
    }

    public String getSenha() {
    return senha;
    }

    public void setSenha(String senha) {
    this.senha = senha;
  }

  public Cargo getCargo() {return cargo;}

  public void setCargo(Cargo cargo) {this.cargo = cargo;}

  public LocalDate getCriadoEm() {return criadoEm;}

  public void setCriadoEm(LocalDate criadoEm) {this.criadoEm = criadoEm;}

  public boolean isAtivo() {return ativo;}

  public void setAtivo(boolean ativo) {this.ativo = ativo;}
}