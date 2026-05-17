package br.com.sgpj.entity.onedrive;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "microsoft_token_config")
public class MicrosoftTokenConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String refreshToken;
    private LocalDateTime dataCriacao;

    public MicrosoftTokenConfig() {}

    public MicrosoftTokenConfig(Long id, String refreshToken, LocalDateTime dataCriacao) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
