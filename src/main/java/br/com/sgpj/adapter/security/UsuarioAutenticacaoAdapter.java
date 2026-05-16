package br.com.sgpj.adapter.security;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAutenticacaoAdapter {

    private final AuthenticationManager authenticationManager;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;

    public UsuarioAutenticacaoAdapter(
            AuthenticationManager authenticationManager,
            GerenciadorTokenJwt gerenciadorTokenJwt) {
        this.authenticationManager = authenticationManager;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
    }

    public Authentication autenticar(String email, String senha) {
        UsernamePasswordAuthenticationToken credenciais =
                new UsernamePasswordAuthenticationToken(email, senha);
        return authenticationManager.authenticate(credenciais);
    }

    public void definirContexto(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public String gerarToken(Authentication authentication) {
        return gerenciadorTokenJwt.generateToken(authentication);
    }

    public String gerarTokenOtp(Authentication authentication) {
        return gerenciadorTokenJwt.generateTokenOtp(authentication);
    }

    public boolean isTokenDeRecuperacao(String token) {
        String scope = gerenciadorTokenJwt.getClaimForToken(token, claims -> claims.get("scope", String.class));
        return "SET_PASSWORD".equals(scope);
    }

    public String obterEmailUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Nenhum usuário autenticado encontrado no contexto.");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        else if (principal instanceof String) {
            return (String) principal;
        }

        throw new SecurityException("Não foi possível extrair o e-mail do token.");
    }
}
