package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.PasswordConflictException;
import br.com.sgpj.mapper.UsuarioMapper;
import org.springframework.http.ResponseEntity;
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

  private final PasswordEncoder passwordEncoder;
  private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
  private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
  private final OtpService otpService;
  private final EmailService emailService;

  public UsuarioService(PasswordEncoder passwordEncoder,
                        UsuarioPersistenciaAdapter usuarioPersistenciaAdapter,
                        UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                        OtpService otpService, EmailService emailService) {
    this.passwordEncoder = passwordEncoder;
    this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
    this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    this.otpService = otpService;
    this.emailService = emailService;
  }

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

  public void enviarRedefinirSenhaOtp(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Insira um endereço de e-mail válido");
    }

    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(email);
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

  public void definirNovaSenha(DefinirNovaSenhaRequest request) {
    if (request.email() == null || request.email().isBlank()) {
      throw new IllegalArgumentException("Insira um endereço de e-mail válido");
    }

    Usuario usuario = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(request.email());
    String novaSenhaCriptografada = passwordEncoder.encode(request.senha());
    usuario.setSenha(novaSenhaCriptografada);
    usuarioPersistenciaAdapter.salvar(usuario);
    otpService.removeOtp(request.email());
  }

  public UsuarioTokenResponse autenticarOtp(ValidarOtpRequest request) {
    Usuario usuarioAutenticado = usuarioPersistenciaAdapter.buscarUsuarioPorEmail(request.email());

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
    final String token = usuarioAutenticacaoAdapter.gerarToken(authentication);

    return UsuarioMapper.toTokenDto(usuarioAutenticado, token);
  }

  private boolean hasPassword(Usuario usuario) {
    return usuario.getSenha() != null && !usuario.getSenha().isBlank();
  }
}