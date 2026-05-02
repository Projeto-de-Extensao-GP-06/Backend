package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.UsuarioCriacaoRequest;
import br.com.sgpj.dto.UsuarioListarResponse;
import br.com.sgpj.dto.UsuarioTokenResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.mapper.UsuarioMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

  private final PasswordEncoder passwordEncoder;
  private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
  private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

  public UsuarioService(
          PasswordEncoder passwordEncoder,
          UsuarioPersistenciaAdapter usuarioPersistenciaAdapter,
          UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter) {
    this.passwordEncoder = passwordEncoder;
    this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
    this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
  }

  public void criar(UsuarioCriacaoRequest request) {
    if (usuarioPersistenciaAdapter.existeEmail(request.email())) {
      throw new EmailDuplicadoException(request.email());
    }

    Cargo cargo = usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId());

    Usuario novoUsuario = UsuarioMapper.toEntity(request, cargo);
    String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
    novoUsuario.setSenha(senhaCriptografada);
    usuarioPersistenciaAdapter.salvar(novoUsuario);
  }


  public UsuarioListarResponse atualizarUsuario(UsuarioAtualizacaoRequest request, Long id) {
    Usuario usuarioAtual = usuarioPersistenciaAdapter.buscarUsuarioPorId(id);
    Cargo cargo = usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId());

    Usuario usuarioEditado = UsuarioMapper.toEntity(request, cargo);
    usuarioEditado.setId(id);
    usuarioEditado.setCriadoEm(usuarioAtual.getCriadoEm());
    String senhaCriptografada = passwordEncoder.encode(usuarioEditado.getSenha());
    usuarioEditado.setSenha(senhaCriptografada);
    usuarioPersistenciaAdapter.salvar(usuarioEditado);
    return UsuarioMapper.toListarDto(usuarioEditado);
  }

  public UsuarioTokenResponse autenticar(Usuario usuario) {
    final Authentication authentication = usuarioAutenticacaoAdapter.autenticar(
            usuario.getEmail(), usuario.getSenha());

    Usuario usuarioAutenticado = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail());

    usuarioAutenticacaoAdapter.definirContexto(authentication);

    final String token = usuarioAutenticacaoAdapter.gerarToken(authentication);

    return UsuarioMapper.toTokenDto(usuarioAutenticado, token);
  }

  public List<UsuarioListarResponse> listarTodos() {
    return usuarioPersistenciaAdapter.listarTodos().stream().map(UsuarioMapper::toListarDto).toList();
  }

  public List<UsuarioListarResponse> listarPorFuncao(String funcao) {
    return usuarioPersistenciaAdapter.listarPorFuncao(funcao)
            .stream()
            .map(UsuarioMapper::toListarDto)
            .toList();
  }

  public List<UsuarioListarResponse> buscarPorNomeOuEmail(String pesquisa) {
    return usuarioPersistenciaAdapter.pesquisarPorNomeOuEmail(pesquisa)
            .stream()
            .map(UsuarioMapper::toListarDto)
            .toList();
  }

  public Usuario obterPorId(Long id) {
    return usuarioPersistenciaAdapter.buscarUsuarioPorId(id);
  }

  public Void deletar(Long id) {
    Usuario user = obterPorId(id);
    user.setAtivo(false);
    usuarioPersistenciaAdapter.salvar(user);
    return null;
  }
}