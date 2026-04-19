package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cargo_permissao")
public class CargoPermissao {
    @EmbeddedId
    private CargoPermissaoId id;

    @ManyToOne
    @MapsId("cargoId")
    @JoinColumn(name = "cargo_id")
    private Cargo cargo;

    @ManyToOne
    @MapsId("permissaoId")
    @JoinColumn(name = "permissao_id")
    private Permissao permissao;

    @ManyToOne
    @JoinColumn(name = "atribuido_por")
    private Usuario atribuidoPor;

    @Column(nullable = false)
    private LocalDateTime dataHoraAtribuicao;

    // Constructors
    public CargoPermissao() {}

    public CargoPermissao(CargoPermissaoId id, Cargo cargo, Permissao permissao, Usuario atribuidoPor, LocalDateTime dataHoraAtribuicao) {
        this.id = id;
        this.cargo = cargo;
        this.permissao = permissao;
        this.atribuidoPor = atribuidoPor;
        this.dataHoraAtribuicao = dataHoraAtribuicao;
    }

    // Getters and Setters
    public CargoPermissaoId getId() {
        return id;
    }

    public void setId(CargoPermissaoId id) {
        this.id = id;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Permissao getPermissao() {
        return permissao;
    }

    public void setPermissao(Permissao permissao) {
        this.permissao = permissao;
    }

    public Usuario getAtribuidoPor() {
        return atribuidoPor;
    }

    public void setAtribuidoPor(Usuario atribuidoPor) {
        this.atribuidoPor = atribuidoPor;
    }

    public LocalDateTime getDataHoraAtribuicao() {
        return dataHoraAtribuicao;
    }

    public void setDataHoraAtribuicao(LocalDateTime dataHoraAtribuicao) {
        this.dataHoraAtribuicao = dataHoraAtribuicao;
    }
}
