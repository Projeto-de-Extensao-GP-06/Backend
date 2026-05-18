package br.com.sgpj.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = true, length = 250)
    private String senha;

  @ManyToOne
  @JoinColumn(name = "cargo_id")
  private Cargo cargo;

  private LocalDateTime criadoEm;

  private boolean ativo;

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

  public Usuario(Long id, String nome, String email, String senha, Cargo cargo, LocalDateTime criadoEm, boolean ativo) {
    this.id = id;
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.cargo = cargo;
    this.criadoEm = criadoEm;
    this.ativo = ativo;
  }

  public Usuario(String nome, String email, String senha, Cargo cargo, boolean ativo) {
    this.nome = nome;
    this.email = email;
    this.senha = senha;
    this.cargo = cargo;
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

  public LocalDateTime getCriadoEm() {return criadoEm;}

  public void setCriadoEm(LocalDateTime criadoEm) {this.criadoEm = criadoEm;}

  public boolean isAtivo() {return ativo;}

  public void setAtivo(boolean ativo) {this.ativo = ativo;}
}