package br.com.sgpj.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_otp")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiraEm);
    }
}
