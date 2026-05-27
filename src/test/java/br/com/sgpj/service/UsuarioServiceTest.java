package br.com.sgpj.service;

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
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock
    private OtpService otpService;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpServletRequest httpServletRequest;

    private Usuario usuario;
    private Cargo cargo;

    @BeforeEach
    void setUp() {
        cargo = new Cargo();
        cargo.setId(1L);
        cargo.setNome("ADVOGADO");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Test User");
        usuario.setEmail("test@example.com");
        usuario.setSenha("password");
        usuario.setAtivo(true);
        usuario.setCargo(cargo);
        usuario.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        UsuarioCriacaoRequest request = new UsuarioCriacaoRequest("New User", "new@example.com", 1L);
        when(usuarioPersistenciaAdapter.existeEmail(request.email())).thenReturn(false);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId())).thenReturn(cargo);

        assertDoesNotThrow(() -> usuarioService.criar(request));

        verify(usuarioPersistenciaAdapter, times(1)).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar EmailDuplicadoException ao tentar criar usuário com e-mail existente")
    void deveLancarEmailDuplicadoExceptionAoCriarUsuario() {
        UsuarioCriacaoRequest request = new UsuarioCriacaoRequest("New User", "existente@example.com", 1L);
        when(usuarioPersistenciaAdapter.existeEmail(request.email())).thenReturn(true);

        assertThrows(EmailDuplicadoException.class, () -> usuarioService.criar(request));
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void deveAtualizarUsuarioComSucesso() {
        UsuarioAtualizacaoRequest request = new UsuarioAtualizacaoRequest("Updated User", "test@example.com", "newpass", 1L, true);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(1L)).thenReturn(usuario);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(request.cargoId())).thenReturn(cargo);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UsuarioListarResponse response = usuarioService.atualizarUsuario(request, 1L);

        assertNotNull(response);
        assertEquals("Updated User", response.nome());
        verify(usuarioPersistenciaAdapter, times(1)).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve autenticar um usuário com sucesso")
    void deveAutenticarUsuarioComSucesso() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getSenha());
        when(usuarioAutenticacaoAdapter.autenticar(usuario.getEmail(), usuario.getSenha())).thenReturn(authentication);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioAutenticacaoAdapter.gerarToken(authentication)).thenReturn("jwt-token");

        UsuarioTokenResponse response = usuarioService.autenticar(usuario);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals(usuario.getNome(), response.nome());
    }

    @Test
    @DisplayName("Deve lançar UsuarioInativoException ao tentar autenticar um usuário inativo")
    void deveLancarUsuarioInativoExceptionAoAutenticar() {
        usuario.setAtivo(false);
        assertThrows(UsuarioInativoException.class, () -> usuarioService.autenticar(usuario));
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosOsUsuarios() {
        when(usuarioPersistenciaAdapter.listarTodos()).thenReturn(Collections.singletonList(usuario));
        List<UsuarioListarResponse> response = usuarioService.listarTodos();
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Deve desativar um usuário ao deletar")
    void deveDesativarUsuarioAoDeletar() {
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(1L)).thenReturn(usuario);
        usuarioService.deletar(1L);
        verify(usuarioPersistenciaAdapter, times(1)).salvar(argThat(user -> !user.isAtivo()));
    }

    @Test
    @DisplayName("Deve enviar OTP para redefinir a senha com sucesso")
    void deveEnviarOtpParaRedefinirSenha() {
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(otpService.generateOtp(usuario.getEmail())).thenReturn("123456");

        usuarioService.enviarRedefinirSenhaOtp(usuario.getEmail());

        verify(emailService, times(1)).sendForgotPasswordOtp(usuario.getEmail(), usuario.getNome(), "123456");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve lançar IllegalArgumentException se o e-mail for inválido ao enviar OTP para redefinir senha")
    void deveLancarExceptionSeEmailInvalidoAoEnviarOtpRedefinirSenha(String email) {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.enviarRedefinirSenhaOtp(email));
    }

    @Test
    @DisplayName("Deve lançar UsuarioInativoException se o usuário estiver inativo ao enviar OTP para redefinir senha")
    void deveLancarExceptionSeUsuarioInativoAoEnviarOtpRedefinirSenha() {
        usuario.setAtivo(false);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        assertThrows(UsuarioInativoException.class, () -> usuarioService.enviarRedefinirSenhaOtp(usuario.getEmail()));
    }

    @Test
    @DisplayName("Deve lançar PasswordConflictException se o usuário não tiver senha ao enviar OTP para redefinir senha")
    void deveLancarExceptionSeUsuarioNaoTemSenhaAoEnviarOtpRedefinirSenha() {
        usuario.setSenha(null);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        assertThrows(PasswordConflictException.class, () -> usuarioService.enviarRedefinirSenhaOtp(usuario.getEmail()));
    }

    @Test
    @DisplayName("Deve enviar OTP para definir a senha com sucesso")
    void deveEnviarOtpParaDefinirSenha() {
        usuario.setSenha(null);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(otpService.generateOtp(usuario.getEmail())).thenReturn("123456");

        usuarioService.enviarDefinirSenhaOtp(usuario.getEmail());

        verify(emailService, times(1)).sendSetPasswordOtp(usuario.getEmail(), usuario.getNome(), "123456");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve lançar IllegalArgumentException se o e-mail for inválido ao enviar OTP para definir senha")
    void deveLancarExceptionSeEmailInvalidoAoEnviarOtpDefinirSenha(String email) {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.enviarDefinirSenhaOtp(email));
    }

    @Test
    @DisplayName("Deve lançar UsuarioInativoException se o usuário estiver inativo ao enviar OTP para definir senha")
    void deveLancarExceptionSeUsuarioInativoAoEnviarOtpDefinirSenha() {
        usuario.setAtivo(false);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        assertThrows(UsuarioInativoException.class, () -> usuarioService.enviarDefinirSenhaOtp(usuario.getEmail()));
    }

    @Test
    @DisplayName("Deve lançar PasswordConflictException se o usuário já tiver senha ao enviar OTP para definir senha")
    void deveLancarExceptionSeUsuarioJaTemSenhaAoEnviarOtpDefinirSenha() {
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        assertThrows(PasswordConflictException.class, () -> usuarioService.enviarDefinirSenhaOtp(usuario.getEmail()));
    }

    @Test
    @DisplayName("Deve retornar true para um OTP válido")
    void deveRetornarTrueParaOtpValido() {
        ValidarOtpRequest request = new ValidarOtpRequest("test@example.com", "123456");
        when(otpService.isOtpValid(request.email(), request.otp())).thenReturn(true);
        assertTrue(usuarioService.validarOtp(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve lançar IllegalArgumentException se o e-mail for inválido ao validar OTP")
    void deveLancarExceptionSeEmailInvalidoAoValidarOtp(String email) {
        ValidarOtpRequest request = new ValidarOtpRequest(email, "123456");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.validarOtp(request));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve lançar IllegalArgumentException se o OTP for inválido ao validar OTP")
    void deveLancarExceptionSeOtpInvalidoAoValidarOtp(String otp) {
        ValidarOtpRequest request = new ValidarOtpRequest("test@example.com", otp);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.validarOtp(request));
    }

    @Test
    @DisplayName("Deve definir uma nova senha com sucesso")
    void deveDefinirNovaSenhaComSucesso() {
        String newPassword = "newPassword";
        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(usuario.getEmail());
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        usuarioService.definirNovaSenha(newPassword);

        verify(usuarioPersistenciaAdapter, times(1)).salvar(argThat(user -> user.getSenha().equals("encodedNewPassword")));
        verify(otpService, times(1)).removeOtp(usuario.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve lançar IllegalArgumentException se a senha for inválida ao definir nova senha")
    void deveLancarExceptionSeSenhaInvalidaAoDefinirNovaSenha(String senha) {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.definirNovaSenha(senha));
    }

    @Test
    @DisplayName("Deve autenticar com OTP com sucesso")
    void deveAutenticarComOtpComSucesso() {
        ValidarOtpRequest request = new ValidarOtpRequest(usuario.getEmail(), "123456");
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(request.email())).thenReturn(usuario);
        when(usuarioAutenticacaoAdapter.gerarTokenOtp(any(Authentication.class))).thenReturn("jwt-otp-token");

        UsuarioTokenResponse response = usuarioService.autenticarOtp(request);

        assertNotNull(response);
        assertEquals("jwt-otp-token", response.token());
    }

    @Test
    @DisplayName("Deve lançar UsuarioInativoException ao autenticar com OTP se o usuário estiver inativo")
    void deveLancarUsuarioInativoExceptionAoAutenticarComOtp() {
        usuario.setAtivo(false);
        ValidarOtpRequest request = new ValidarOtpRequest(usuario.getEmail(), "123456");
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(request.email())).thenReturn(usuario);

        assertThrows(UsuarioInativoException.class, () -> usuarioService.autenticarOtp(request));
    }

    @Test
    @DisplayName("Deve reativar um usuário com sucesso")
    void deveReativarUsuarioComSucesso() {
        usuario.setAtivo(false);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        UsuarioListarResponse response = usuarioService.reativarUsuario(usuario.getEmail());

        assertTrue(response.ativo());
        verify(usuarioPersistenciaAdapter, times(1)).salvar(argThat(Usuario::isAtivo));
    }

    @Test
    @DisplayName("Deve lançar UsuarioJaAtivoException ao tentar reativar um usuário já ativo")
    void deveLancarUsuarioJaAtivoExceptionAoReativar() {
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        assertThrows(UsuarioJaAtivoException.class, () -> usuarioService.reativarUsuario(usuario.getEmail()));
    }

    @Test
    @DisplayName("Deve listar usuários por função")
    void deveListarUsuariosPorFuncao() {
        String funcao = "ADVOGADO";
        when(usuarioPersistenciaAdapter.listarPorFuncao(funcao)).thenReturn(Collections.singletonList(usuario));

        List<UsuarioListarResponse> response = usuarioService.listarPorFuncao(funcao);

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        verify(usuarioPersistenciaAdapter, times(1)).listarPorFuncao(funcao);
    }

    @Test
    @DisplayName("Deve buscar usuários por nome ou e-mail")
    void deveBuscarUsuariosPorNomeOuEmail() {
        String pesquisa = "test";
        when(usuarioPersistenciaAdapter.pesquisarPorNomeOuEmail(pesquisa)).thenReturn(Collections.singletonList(usuario));

        List<UsuarioListarResponse> response = usuarioService.buscarPorNomeOuEmail(pesquisa);

        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        verify(usuarioPersistenciaAdapter, times(1)).pesquisarPorNomeOuEmail(pesquisa);
    }

    @Test
    @DisplayName("Deve retornar true se o escopo estiver correto")
    void deveRetornarTrueSeEscopoCorreto() {
        String scope = "SCOPE_USER";
        when(usuarioAutenticacaoAdapter.isCorrectScope(httpServletRequest, scope)).thenReturn(true);

        boolean result = usuarioService.isCorrectScope(httpServletRequest, scope);

        assertTrue(result);
        verify(usuarioAutenticacaoAdapter, times(1)).isCorrectScope(httpServletRequest, scope);
    }

    @Test
    @DisplayName("Deve retornar false se o escopo estiver incorreto")
    void deveRetornarFalseSeEscopoIncorreto() {
        String scope = "SCOPE_ADMIN";
        when(usuarioAutenticacaoAdapter.isCorrectScope(httpServletRequest, scope)).thenReturn(false);

        boolean result = usuarioService.isCorrectScope(httpServletRequest, scope);

        assertFalse(result);
        verify(usuarioAutenticacaoAdapter, times(1)).isCorrectScope(httpServletRequest, scope);
    }
}
