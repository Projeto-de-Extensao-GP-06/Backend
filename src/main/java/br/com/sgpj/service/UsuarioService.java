package br.com.sgpj.service;

import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.UsuarioAlterarSenhaRequest;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioSessaoResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.service.useCases.usuario.AlterarMinhaSenha;
import br.com.sgpj.service.useCases.usuario.AutenticarOtp;
import br.com.sgpj.service.useCases.usuario.AutenticarUsuario;
import br.com.sgpj.service.useCases.usuario.CreateUsuario;
import br.com.sgpj.service.useCases.usuario.DefinirNovaSenha;
import br.com.sgpj.service.useCases.usuario.DeleteUsuario;
import br.com.sgpj.service.useCases.usuario.EnviarDefinirSenhaOtp;
import br.com.sgpj.service.useCases.usuario.EnviarRedefinirSenhaOtp;
import br.com.sgpj.service.useCases.usuario.ReadUsuario;
import br.com.sgpj.service.useCases.usuario.ReativarUsuario;
import br.com.sgpj.service.useCases.usuario.UpdateUsuario;
import br.com.sgpj.service.useCases.usuario.ValidarOtp;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

  private final CreateUsuario createUsuario;
  private final ReadUsuario readUsuario;
  private final UpdateUsuario updateUsuario;
  private final DeleteUsuario deleteUsuario;
  private final ReativarUsuario reativarUsuario;

  private final AutenticarUsuario autenticarUsuario;
  private final AutenticarOtp autenticarOtp;
  private final EnviarRedefinirSenhaOtp enviarRedefinirSenhaOtp;
  private final EnviarDefinirSenhaOtp enviarDefinirSenhaOtp;
  private final ValidarOtp validarOtp;
  private final DefinirNovaSenha definirNovaSenha;
  private final AlterarMinhaSenha alterarMinhaSenha;

  public UsuarioService(
          CreateUsuario createUsuario,
          ReadUsuario readUsuario,
          UpdateUsuario updateUsuario,
          DeleteUsuario deleteUsuario,
          ReativarUsuario reativarUsuario,
          AutenticarUsuario autenticarUsuario,
          AutenticarOtp autenticarOtp,
          EnviarRedefinirSenhaOtp enviarRedefinirSenhaOtp,
          EnviarDefinirSenhaOtp enviarDefinirSenhaOtp,
          ValidarOtp validarOtp,
          DefinirNovaSenha definirNovaSenha,
          AlterarMinhaSenha alterarMinhaSenha
  ) {
    this.createUsuario = createUsuario;
    this.readUsuario = readUsuario;
    this.updateUsuario = updateUsuario;
    this.deleteUsuario = deleteUsuario;
    this.reativarUsuario = reativarUsuario;
    this.autenticarUsuario = autenticarUsuario;
    this.autenticarOtp = autenticarOtp;
    this.enviarRedefinirSenhaOtp = enviarRedefinirSenhaOtp;
    this.enviarDefinirSenhaOtp = enviarDefinirSenhaOtp;
    this.validarOtp = validarOtp;
    this.definirNovaSenha = definirNovaSenha;
    this.alterarMinhaSenha = alterarMinhaSenha;
  }

  // =========================
  // CREATE
  // =========================

  public void criar(UsuarioCriacaoRequest request) {
    createUsuario.executar(request);
  }

  // =========================
  // READ
  // =========================

  public List<UsuarioListarResponse> listarTodos() {
    return readUsuario.listarTodos();
  }

  public List<UsuarioListarResponse> listarPorCargo(String cargo) {
    return readUsuario.listarPorCargo(cargo);
  }

  public List<UsuarioListarResponse> buscarPorNomeOuEmail(
          String pesquisa
  ) {
    return readUsuario.buscarPorNomeOuEmail(pesquisa);
  }

  public Usuario obterPorId(Long id) {
    return readUsuario.obterPorId(id);
  }

  // =========================
  // UPDATE
  // =========================

  public UsuarioListarResponse atualizarUsuario(
          Long id,
          UsuarioAtualizacaoRequest request
  ) {
    return updateUsuario.executar(request, id);
  }

  public UsuarioListarResponse reativarUsuario(Long id) {
    return reativarUsuario.executar(id);
  }

  // =========================
  // DELETE
  // =========================

  public void deletar(Long id) {
    deleteUsuario.executar(id);
  }

  // =========================
  // AUTENTICAÇÃO
  // =========================

  public UsuarioTokenResponse autenticar(UsuarioLoginRequest usuario) {
    AutenticacaoUsuarioResponse autenticacao = autenticarUsuario.executar(usuario);
    return autenticacao.tokenResponse();
  }

  public UsuarioSessaoResponse autenticar(String email) {
    return autenticarUsuario.obterSessao(email);
  }

  public UsuarioTokenResponse autenticarOtp(
          ValidarOtpRequest request
  ) {
    AutenticacaoUsuarioResponse autenticacao = autenticarOtp.executar(request);
    return autenticacao.tokenResponse();
  }

  // =========================
  // OTP
  // =========================

  public void enviarRedefinirSenhaOtp(String email) {
    enviarRedefinirSenhaOtp.executar(email);
  }

  public void enviarDefinirSenhaOtp(String email) {
    enviarDefinirSenhaOtp.executar(email);
  }

  public void validarOtp(ValidarOtpRequest request) {
    validarOtp.executar(request);
  }

  // =========================
  // SENHA
  // =========================

  public void definirNovaSenha(DefinirNovaSenhaRequest request) {
    definirNovaSenha.executar(request);
  }

  public void alterarMinhaSenha(UsuarioAlterarSenhaRequest request) {
    alterarMinhaSenha.executar(request);
  }
}
