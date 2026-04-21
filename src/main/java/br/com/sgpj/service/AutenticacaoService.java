package br.com.sgpj.service;

import br.com.sgpj.dto.UsuarioDetalhesResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

  private final UsuarioRepository usuarioRepository;

  public AutenticacaoService(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  // Metodo da interface implementada
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario = usuarioRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                    String.format("usuário: %s não encontrado", username)));

    return new UsuarioDetalhesResponse(usuario);
  }
}
