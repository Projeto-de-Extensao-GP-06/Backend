package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.response.UsuarioDetalhesResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService implements UserDetailsService {

  private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;

  public AutenticacaoService(UsuarioPersistenciaAdapter usuarioPersistenciaAdapter) {
    this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
  }

  // Metodo da interface implementada
  @Override
  @Transactional(readOnly = true) // mantém a sessão aberta durante o loadAuthorities
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Usuario usuario;

    try {
      usuario = usuarioPersistenciaAdapter.findByEmailWithPermissoes(username);
    } catch (UsuarioNaoEncontradoException exception) {
      throw new UsernameNotFoundException(String.format("usuário: %s não encontrado", username), exception);
    }

    return new UsuarioDetalhesResponse(usuario);
  }
}
