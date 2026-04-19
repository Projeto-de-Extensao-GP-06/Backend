package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class CargoPermissaoId implements Serializable {
    @Column(name = "cargo_id")
    private Long cargoId;

    @Column(name = "permissao_id")
    private Long permissaoId;

    public CargoPermissaoId(){};

    public CargoPermissaoId(Long cargoId, Long permissaoId) {
        this.cargoId = cargoId;
        this.permissaoId = permissaoId;
    }

    public Long getCargoId() {
        return cargoId;
    }

    public void setCargoId(Long cargoId) {
        this.cargoId = cargoId;
    }

    public Long getPermissaoId() {
        return permissaoId;
    }

    public void setPermissaoId(Long permissaoId) {
        this.permissaoId = permissaoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CargoPermissaoId that = (CargoPermissaoId) o;
        return Objects.equals(cargoId, that.cargoId) &&
                Objects.equals(permissaoId, that.permissaoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cargoId, permissaoId);
    }
}
