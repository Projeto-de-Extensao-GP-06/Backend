package br.com.sgpj.service;

import br.com.sgpj.dto.response.Usuario.UsuarioDetalhesResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;

  @Value("${jwt.secret}")
  private String jwtSecret;

  public AutenticacaoService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  // Metodo da interface implementada
  @Override
  @Transactional(readOnly = true) // mantém a sessão aberta durante o loadAuthorities
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

      Usuario usuario = usuarioRepository.findByEmailWithPermissoes(username)
              .orElseThrow(() -> new UsuarioNaoEncontradoException(username));


    return new UsuarioDetalhesResponse(usuario);
  }

  public String getScope(String token) {
    try {
      // Se usar JJWT versão mais recente (0.11.x ou superior)
      Claims claims = Jwts.parser()
              .setSigningKey(jwtSecret)
              .build()
              .parseClaimsJws(token)
              .getBody();

      return claims.get("scope", String.class);
    } catch (Exception e) {
      return null;
    }
  }
}
