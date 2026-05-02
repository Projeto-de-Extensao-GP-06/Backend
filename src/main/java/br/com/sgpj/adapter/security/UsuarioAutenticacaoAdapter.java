package br.com.sgpj.adapter.security;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
}
