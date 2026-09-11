package br.com.sgpj.repository;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.UsuarioRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsuarioRefreshTokenRepository extends JpaRepository<UsuarioRefreshToken, Long> {
    Optional<UsuarioRefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);

    Optional<UsuarioRefreshToken> findByTokenAndRevogadoFalse(String token);

    void deleteByExpiraEmBefore(LocalDateTime dataHora);
}