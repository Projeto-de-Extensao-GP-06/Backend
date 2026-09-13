package br.com.sgpj.entity.Processo;

import jakarta.persistence.*;

@Entity
@Table(name = "motivo_status_processo")
public class MotivoStatusProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplicavel_a")
    private AplicavelA aplicavelA;

    public enum AplicavelA {
        SUSPENSO,
        ENCERRADO,
        AMBOS
    }

    public MotivoStatusProcesso() {}

    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public AplicavelA getAplicavelA() { return aplicavelA; }

    public void setId(Integer id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAplicavelA(AplicavelA aplicavelA) { this.aplicavelA = aplicavelA; }
}
