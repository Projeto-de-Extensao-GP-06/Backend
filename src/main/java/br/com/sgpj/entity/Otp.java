package br.com.sgpj.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "tentativas", nullable = false)
    private int tentativas = 0;

    public Otp(String email, String otp, LocalDateTime expiraEm) {
        this.email = email;
        this.otp = otp;
        this.expiraEm = expiraEm;
        this.tentativas = 0;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiraEm);
    }

    public void registrarTentativaIncorreta() {
        this.tentativas++;
    }

    public boolean excedeuLimiteDeTentativas(int maxTentativas) {
        return this.tentativas >= maxTentativas;
    }
}
