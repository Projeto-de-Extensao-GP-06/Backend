package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_otp")
public class Otp {

    @Id
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "otp", nullable = false, length = 6)
    private String otp;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "tentativas", nullable = false)
    private int tentativas = 0;

    public Otp() {
    }

    public Otp(String email, String otp, LocalDateTime expiraEm) {
        this.email = email;
        this.otp = otp;
        this.expiraEm = expiraEm;
        this.tentativas = 0;
    }

    public Otp(String email, String otp, LocalDateTime expiraEm, int tentativas) {
        this.email = email;
        this.otp = otp;
        this.expiraEm = expiraEm;
        this.tentativas = tentativas;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
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
