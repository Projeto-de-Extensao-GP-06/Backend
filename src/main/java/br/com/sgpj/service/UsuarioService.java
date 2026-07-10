package br.com.sgpj.service;

import br.com.sgpj.adapter.database.CargoPersistenciaAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.PasswordConflictException;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.exception.UsuarioJaAtivoException;
import br.com.sgpj.mapper.UsuarioMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UsuarioService {

  public UsuarioService(PasswordEncoder passwordEncoder, UsuarioPersistenciaAdapter usuarioPersistenciaAdapter, UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter, OtpService otpService, EmailService emailService, CargoPersistenciaAdapter cargoPersistenciaAdapter) {
    this.passwordEncoder = passwordEncoder;
    this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
    this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    this.otpService = otpService;
    this.emailService = emailService;
    this.cargoPersistenciaAdapter = cargoPersistenciaAdapter;
  }

  private final PasswordEncoder passwordEncoder;
  private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
  private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
  private final OtpService otpService;
  private final EmailService emailService;
  private final CargoPersistenciaAdapter cargoPersistenciaAdapter;

  public void criar(UsuarioCriacaoRequest request) {
    if (usuarioPersistenciaAdapter.existeEmail(request.email())) {
      throw new EmailDuplicadoException(request.email());
    }

    Cargo cargo = usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId());

    Usuario novoUsuario = UsuarioMapper.toEntity(request, cargo);
    usuarioPersistenciaAdapter.salvar(novoUsuario);
  }


  public UsuarioListarResponse atualizarUsuario(UsuarioAtualizacaoRequest request, Long id) {
    Usuario usuarioAtual = usuarioPersistenciaAdapter.buscarUsuarioPorId(id);
    Cargo cargo = usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId());

    Usuario usuarioEditado = UsuarioMapper.toEntity(request, cargo);
    usuarioEditado.setId(id);
    usuarioEditado.setCriadoEm(usuarioAtual.getCriadoEm());
    usuarioEditado.setSenha(usuarioAtual.getSenha());
    usuarioPersistenciaAdapter.salvar(usuarioEditado);
    return UsuarioMapper.toListarDto(usuarioEditado);
  }

  public UsuarioTokenResponse autenticar(Usuario usuario) {
    Usuario usuarioAutenticado = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail());

    if (!usuarioAutenticado.isAtivo()) {
      throw new UsuarioInativoException("Este usuário está inativo. Contate um administrador.");
    }

    final Authentication authentication = usuarioAutenticacaoAdapter.autenticar(
            usuario.getEmail(), usuario.getSenha());

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

  public boolean isDifferentId(Long idExcluir) {
    String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(email);
    return !usuario.getId().equals(idExcluir);
  }

  public Void deletar(Long id) {
    Usuario user = obterPorId(id);
    user.setAtivo(false);
    Cargo semCargo = cargoPersistenciaAdapter.buscarPorNome("Sem Cargo");
    user.setCargo(semCargo);
    usuarioPersistenciaAdapter.salvar(user);
    return null;
  }

  public void enviarRedefinirSenhaOtp(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Insira um endereço de e-mail válido");
    }

    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(email);
    if (!usuario.isAtivo()) {
      throw new UsuarioInativoException("Este usuário está inativo. Contate um administrador.");
    }

    if (!hasPassword(usuario)) {
      throw new PasswordConflictException("Essa conta ainda não realizou o primeiro acesso no sistema e não possui uma senha ativa.");
    }

    String otp = otpService.generateOtp(email);
    emailService.sendForgotPasswordOtp(email, usuario.getNome(), otp);
  }

  public void enviarDefinirSenhaOtp(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Insira um endereço de e-mail válido");
    }

    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(email);
    if (!usuario.isAtivo()) {
      throw new UsuarioInativoException("Este usuário está inativo. Contate um administrador.");
    }

    if (hasPassword(usuario)) {
      throw new PasswordConflictException("Esta conta já possui uma senha cadastrada. Utilize a opção de recuperação de senha.");
    }

    String otp = otpService.generateOtp(email);
    emailService.sendSetPasswordOtp(email, usuario.getNome(), otp);
  }

  public boolean validarOtp(ValidarOtpRequest request) {
    if (request.email() == null || request.email().isBlank()) {
      throw new IllegalArgumentException("Insira um endereço de e-mail válido");
    }

    if (request.otp() == null || request.otp().isBlank()) {
      throw new IllegalArgumentException("Insira uma OTP válida.");
    }

    return otpService.isOtpValid(request.email(), request.otp());
  }

  public boolean isCorrectScope(HttpServletRequest httpRequest, String scope) {
    return usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope);
  }

  public void definirNovaSenha(String senha) {
    if (senha == null || senha.isBlank()) {
      throw new IllegalArgumentException("Insira uma senha válida");
    }

    String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(email);
    String novaSenhaCriptografada = passwordEncoder.encode(senha);
    usuario.setSenha(novaSenhaCriptografada);
    usuarioPersistenciaAdapter.salvar(usuario);
    otpService.removeOtp(email);
  }

  public UsuarioTokenResponse autenticarOtp(ValidarOtpRequest request) {
    Usuario usuarioAutenticado = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(request.email());

    if (!usuarioAutenticado.isAtivo()) {
      throw new UsuarioInativoException("Este usuário está inativo. Contate um administrador.");
    }

    UserDetails userDetails = User.withUsername(request.email())
            .password("")
            .authorities(Collections.emptyList())
            .build();

    Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
    );
    usuarioAutenticacaoAdapter.definirContexto(authentication);
    final String token = usuarioAutenticacaoAdapter.gerarTokenOtp(authentication);

    return UsuarioMapper.toTokenDto(usuarioAutenticado, token);
  }

  public UsuarioListarResponse reativarUsuario(Long id) {
    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorId(id);

    if (usuario.isAtivo()) {
      throw new UsuarioJaAtivoException("Usuário já está ativo.");
    }

    usuario.setAtivo(true);
    usuarioPersistenciaAdapter.salvar(usuario);
    return UsuarioMapper.toListarDto(usuario);
  }

  private boolean hasPassword(Usuario usuario) {
    return usuario.getSenha() != null && !usuario.getSenha().isBlank();
  }
}