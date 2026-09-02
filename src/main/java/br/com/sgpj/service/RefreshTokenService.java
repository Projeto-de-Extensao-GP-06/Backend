package br.com.sgpj.service;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import br.com.sgpj.dto.response.RefreshTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.UsuarioRefreshToken;
import br.com.sgpj.exception.TokenInvalidoException;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.repository.UsuarioRefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh.validity}")
    private Integer REFRESH_TOKEN_VALIDITY;

    private final UsuarioRefreshTokenRepository usuarioRefreshTokenRepository;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final AutenticacaoService autenticacaoService;

    @Transactional
    public String criarRefreshToken(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        String tokenHash = calcularHash(token);

        UsuarioRefreshToken refreshToken = UsuarioRefreshToken.builder()
                .token(tokenHash)
                .usuario(usuario)
                .criadoEm(LocalDateTime.now())
                .revogado(false)
                .expiraEm(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_VALIDITY))
                .build();

        usuarioRefreshTokenRepository.save(refreshToken);
        return token;
    }

    @Transactional
    public RefreshTokenResponse validarERotacionar(String tokenString) {
        String tokenHash = calcularHash(tokenString);
        UsuarioRefreshToken refreshToken = usuarioRefreshTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido"));

        if (Boolean.TRUE.equals(refreshToken.getRevogado())) {
            // Potencial ataque: revoga todas as sessões do usuário
            usuarioRefreshTokenRepository.deleteByUsuario(refreshToken.getUsuario());
            throw new TokenInvalidoException("Tentativa de reutilização de refresh token revogado.");
        }

        Usuario usuario = refreshToken.getUsuario();

        if (!usuario.isAtivo()) {
            refreshToken.setRevogado(true);
            throw new UsuarioInativoException("Este usuário está inativo.");
        }

        if (refreshToken.getExpiraEm().isBefore(LocalDateTime.now())) {
            refreshToken.setRevogado(true);
            usuarioRefreshTokenRepository.save(refreshToken);
            throw new TokenInvalidoException("Refresh token inválido");
        }

        refreshToken.setRevogado(true);
        usuarioRefreshTokenRepository.save(refreshToken);

        String novoRefreshToken = criarRefreshToken(usuario);

        UserDetails userDetails = autenticacaoService.loadUserByUsername(usuario.getEmail());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        String novoAccessToken = gerenciadorTokenJwt.generateToken(authentication);

        return new RefreshTokenResponse(novoAccessToken, novoRefreshToken);
    }

    @Transactional
    public void revogarToken(String tokenString) {
        String tokenHash = calcularHash(tokenString);
        usuarioRefreshTokenRepository.findByToken(tokenHash)
                .ifPresent(token -> {
                    token.setRevogado(true);
                    usuarioRefreshTokenRepository.save(token);
                });
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void expurgarTokensExpirados() {
        usuarioRefreshTokenRepository.deleteByExpiraEmBefore(LocalDateTime.now());
    }

    private String calcularHash(String token) {
        if (token == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 não disponível", e);
        }
    }
}
