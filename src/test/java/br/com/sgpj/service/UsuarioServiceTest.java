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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

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

    @InjectMocks
    private UsuarioService usuarioService;

    private static final String EMAIL_TESTE = "usuario@teste.com";
    private static final String NOME_TESTE = "João Silva";
    private static final String SENHA_TESTE = "senha123";
    private static final String OTP_TESTE = "123456";
    private static final Long ID_TESTE = 1L;

    // ========================
    // criar()
    // ========================

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        // Given
        UsuarioCriacaoRequest request = mock(UsuarioCriacaoRequest.class);
        Cargo cargo = mock(Cargo.class);
        when(request.email()).thenReturn(EMAIL_TESTE);
        when(request.cargoId()).thenReturn(ID_TESTE);
        when(usuarioPersistenciaAdapter.existeEmail(EMAIL_TESTE)).thenReturn(false);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);

        // When
        usuarioService.criar(request);

        // Then
        verify(usuarioPersistenciaAdapter, times(1)).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void deveLancarExcecaoAoCriarUsuarioComEmailDuplicado() {
        // Given
        UsuarioCriacaoRequest request = mock(UsuarioCriacaoRequest.class);
        when(request.email()).thenReturn(EMAIL_TESTE);
        when(usuarioPersistenciaAdapter.existeEmail(EMAIL_TESTE)).thenReturn(true);

        // When/Then
        assertThrows(
                EmailDuplicadoException.class,
                () -> usuarioService.criar(request)
        );

        verify(usuarioPersistenciaAdapter, never()).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar cargo ao criar usuário")
    void deveBuscarCargoAoCriarUsuario() {
        // Given
        UsuarioCriacaoRequest request = mock(UsuarioCriacaoRequest.class);
        Cargo cargo = mock(Cargo.class);
        when(request.email()).thenReturn(EMAIL_TESTE);
        when(request.cargoId()).thenReturn(ID_TESTE);
        when(usuarioPersistenciaAdapter.existeEmail(EMAIL_TESTE)).thenReturn(false);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);

        // When
        usuarioService.criar(request);

        // Then
        verify(usuarioPersistenciaAdapter, times(1)).buscarCargoPorId(ID_TESTE);
    }

    // ========================
    // atualizarUsuario()
    // ========================

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void deveAtualizarUsuarioComSucesso() {
        // Given
        UsuarioAtualizacaoRequest request = mock(UsuarioAtualizacaoRequest.class);
        Usuario usuarioAtual = criarUsuarioAtivo();
        Cargo cargo = mock(Cargo.class);
        when(request.cargoId()).thenReturn(ID_TESTE);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(ID_TESTE)).thenReturn(usuarioAtual);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);

        // When
        usuarioService.atualizarUsuario(request, ID_TESTE);

        // Then
        verify(usuarioPersistenciaAdapter, times(1)).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve retornar UsuarioListarResponse ao atualizar usuário")
    void deveRetornarUsuarioListarResponseAoAtualizarUsuario() {
        // Given
        UsuarioAtualizacaoRequest request = mock(UsuarioAtualizacaoRequest.class);
        Usuario usuarioAtual = criarUsuarioAtivo();
        Cargo cargo = mock(Cargo.class);
        when(request.cargoId()).thenReturn(ID_TESTE);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(ID_TESTE)).thenReturn(usuarioAtual);
        when(usuarioPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);

        // When
        UsuarioListarResponse resultado = usuarioService.atualizarUsuario(request, ID_TESTE);

        // Then
        assertNotNull(resultado);
    }

    // ========================
    // autenticar()
    // ========================

    @Test
    @DisplayName("Deve autenticar usuário ativo com sucesso")
    void deveAutenticarUsuarioAtivoComSucesso() {
        // Given
        Usuario usuario = criarUsuarioAtivoComSenha(); // <- Usar usuário com senha
        Authentication authentication = mock(Authentication.class);
        String token = "token-jwt";
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(usuarioAutenticacaoAdapter.autenticar(EMAIL_TESTE, SENHA_TESTE)).thenReturn(authentication);
        when(usuarioAutenticacaoAdapter.gerarToken(authentication)).thenReturn(token);

        // When
        UsuarioTokenResponse resultado = usuarioService.autenticar(usuario);

        // Then
        assertNotNull(resultado);
        assertEquals(token, resultado.token());
        assertEquals(usuario.getId(), resultado.id());
        assertEquals(EMAIL_TESTE, resultado.email());
        verify(usuarioAutenticacaoAdapter).autenticar(EMAIL_TESTE, SENHA_TESTE);
        verify(usuarioAutenticacaoAdapter).definirContexto(authentication);
        verify(usuarioAutenticacaoAdapter).gerarToken(authentication);
    }

    @Test
    @DisplayName("Deve lançar exceção ao autenticar usuário inativo")
    void deveLancarExcecaoAoAutenticarUsuarioInativo() {
        // Given
        Usuario usuarioRequest  = criarUsuarioInativo();

        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(usuarioRequest.getEmail()))
                .thenReturn(usuarioRequest);

        // When/Then
        assertThrows(
                UsuarioInativoException.class,
                () -> usuarioService.autenticar(usuarioRequest)
        );

        verify(usuarioAutenticacaoAdapter, never()).autenticar(anyString(), anyString());
        verify(usuarioAutenticacaoAdapter, never()).definirContexto(any());
        verify(usuarioAutenticacaoAdapter, never()).gerarToken(any());
    }

    // ========================
    // listarTodos()
    // ========================

    @Test
    @DisplayName("Deve listar todos os usuários")
    void deveListarTodosOsUsuarios() {
        // Given
        when(usuarioPersistenciaAdapter.listarTodos()).thenReturn(List.of(criarUsuarioAtivo()));

        // When
        List<UsuarioListarResponse> resultado = usuarioService.listarTodos();

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(usuarioPersistenciaAdapter, times(1)).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver usuários")
    void deveRetornarListaVaziaQuandoNaoHouverUsuarios() {
        // Given
        when(usuarioPersistenciaAdapter.listarTodos()).thenReturn(List.of());

        // When
        List<UsuarioListarResponse> resultado = usuarioService.listarTodos();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // ========================
    // listarPorFuncao()
    // ========================

    @Test
    @DisplayName("Deve listar usuários por função")
    void deveListarUsuariosPorFuncao() {
        // Given
        String funcao = "ADMIN";
        when(usuarioPersistenciaAdapter.listarPorFuncao(funcao)).thenReturn(List.of(criarUsuarioAtivo()));

        // When
        List<UsuarioListarResponse> resultado = usuarioService.listarPorFuncao(funcao);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(usuarioPersistenciaAdapter, times(1)).listarPorFuncao(funcao);
    }

    // ========================
    // buscarPorNomeOuEmail()
    // ========================

    @Test
    @DisplayName("Deve buscar usuários por nome ou email")
    void deveBuscarUsuariosPorNomeOuEmail() {
        // Given
        String pesquisa = "João";
        when(usuarioPersistenciaAdapter.pesquisarPorNomeOuEmail(pesquisa)).thenReturn(List.of(criarUsuarioAtivo()));

        // When
        List<UsuarioListarResponse> resultado = usuarioService.buscarPorNomeOuEmail(pesquisa);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(usuarioPersistenciaAdapter, times(1)).pesquisarPorNomeOuEmail(pesquisa);
    }

    // ========================
    // obterPorId()
    // ========================

    @Test
    @DisplayName("Deve obter usuário por id")
    void deveObterUsuarioPorId() {
        // Given
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(ID_TESTE)).thenReturn(usuario);

        // When
        Usuario resultado = usuarioService.obterPorId(ID_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(usuario, resultado);
        verify(usuarioPersistenciaAdapter, times(1)).buscarUsuarioPorId(ID_TESTE);
    }

    // ========================
    // deletar()
    // ========================

    @Test
    @DisplayName("Deve desativar usuário ao deletar")
    void deveDesativarUsuarioAoDeletar() {
        // Given
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(ID_TESTE)).thenReturn(usuario);

        // When
        usuarioService.deletar(ID_TESTE);

        // Then
        assertFalse(usuario.isAtivo());
        verify(usuarioPersistenciaAdapter, times(1)).salvar(usuario);
    }

    @Test
    @DisplayName("Deve retornar null ao deletar")
    void deveRetornarNullAoDeletar() {
        // Given
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(ID_TESTE)).thenReturn(usuario);

        // When
        Void resultado = usuarioService.deletar(ID_TESTE);

        // Then
        assertNull(resultado);
    }

    // ========================
    // enviarRedefinirSenhaOtp()
    // ========================

    @Test
    @DisplayName("Deve enviar OTP de redefinição de senha com sucesso")
    void deveEnviarOtpRedefinicaoSenhaComSucesso() {
        // Given
        Usuario usuario = criarUsuarioAtivoComSenha();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(otpService.generateOtp(EMAIL_TESTE)).thenReturn(OTP_TESTE);

        // When
        usuarioService.enviarRedefinirSenhaOtp(EMAIL_TESTE);

        // Then
        verify(otpService, times(1)).generateOtp(EMAIL_TESTE);
        verify(emailService, times(1)).sendForgotPasswordOtp(EMAIL_TESTE, NOME_TESTE, OTP_TESTE);
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de redefinição com email nulo")
    void deveLancarExcecaoAoEnviarOtpRedefinicaoComEmailNulo() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.enviarRedefinirSenhaOtp(null)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de redefinição com email em branco")
    void deveLancarExcecaoAoEnviarOtpRedefinicaoComEmailEmBranco() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.enviarRedefinirSenhaOtp("   ")
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de redefinição para usuário inativo")
    void deveLancarExcecaoAoEnviarOtpRedefinicaoParaUsuarioInativo() {
        // Given
        Usuario usuario = criarUsuarioInativo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);

        // When/Then
        assertThrows(
                UsuarioInativoException.class,
                () -> usuarioService.enviarRedefinirSenhaOtp(EMAIL_TESTE)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de redefinição para usuário sem senha")
    void deveLancarExcecaoAoEnviarOtpRedefinicaoParaUsuarioSemSenha() {
        // Given
        Usuario usuario = criarUsuarioAtivoSemSenha();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);

        // When/Then
        assertThrows(
                PasswordConflictException.class,
                () -> usuarioService.enviarRedefinirSenhaOtp(EMAIL_TESTE)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    // ========================
    // enviarDefinirSenhaOtp()
    // ========================

    @Test
    @DisplayName("Deve enviar OTP de definição de senha com sucesso")
    void deveEnviarOtpDefinicaoSenhaComSucesso() {
        // Given
        Usuario usuario = criarUsuarioAtivoSemSenha();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(otpService.generateOtp(EMAIL_TESTE)).thenReturn(OTP_TESTE);

        // When
        usuarioService.enviarDefinirSenhaOtp(EMAIL_TESTE);

        // Then
        verify(otpService, times(1)).generateOtp(EMAIL_TESTE);
        verify(emailService, times(1)).sendSetPasswordOtp(EMAIL_TESTE, NOME_TESTE, OTP_TESTE);
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de definição com email nulo")
    void deveLancarExcecaoAoEnviarOtpDefinicaoComEmailNulo() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.enviarDefinirSenhaOtp(null)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de definição com email em branco")
    void deveLancarExcecaoAoEnviarOtpDefinicaoComEmailEmBranco() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.enviarDefinirSenhaOtp("   ")
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de definição para usuário inativo")
    void deveLancarExcecaoAoEnviarOtpDefinicaoParaUsuarioInativo() {
        // Given
        Usuario usuario = criarUsuarioInativo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);

        // When/Then
        assertThrows(
                UsuarioInativoException.class,
                () -> usuarioService.enviarDefinirSenhaOtp(EMAIL_TESTE)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar OTP de definição para usuário que já possui senha")
    void deveLancarExcecaoAoEnviarOtpDefinicaoParaUsuarioComSenha() {
        // Given
        Usuario usuario = criarUsuarioAtivoComSenha();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);

        // When/Then
        assertThrows(
                PasswordConflictException.class,
                () -> usuarioService.enviarDefinirSenhaOtp(EMAIL_TESTE)
        );

        verify(otpService, never()).generateOtp(anyString());
    }

    // ========================
    // validarOtp()
    // ========================

    @Test
    @DisplayName("Deve validar OTP com sucesso")
    void deveValidarOtpComSucesso() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, OTP_TESTE);
        when(otpService.isOtpValid(EMAIL_TESTE, OTP_TESTE)).thenReturn(true);

        // When
        boolean resultado = usuarioService.validarOtp(request);

        // Then
        assertTrue(resultado);
        verify(otpService, times(1)).isOtpValid(EMAIL_TESTE, OTP_TESTE);
    }

    @Test
    @DisplayName("Deve retornar falso para OTP inválido")
    void deveRetornarFalsoParaOtpInvalido() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, OTP_TESTE);
        when(otpService.isOtpValid(EMAIL_TESTE, OTP_TESTE)).thenReturn(false);

        // When
        boolean resultado = usuarioService.validarOtp(request);

        // Then
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar OTP com email nulo")
    void deveLancarExcecaoAoValidarOtpComEmailNulo() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(null, OTP_TESTE);

        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.validarOtp(request)
        );

        verify(otpService, never()).isOtpValid(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar OTP com email em branco")
    void deveLancarExcecaoAoValidarOtpComEmailEmBranco() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest("   ", OTP_TESTE);

        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.validarOtp(request)
        );

        verify(otpService, never()).isOtpValid(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar OTP nulo")
    void deveLancarExcecaoAoValidarOtpNulo() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, null);

        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.validarOtp(request)
        );

        verify(otpService, never()).isOtpValid(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção ao validar OTP em branco")
    void deveLancarExcecaoAoValidarOtpEmBranco() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, "   ");

        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.validarOtp(request)
        );

        verify(otpService, never()).isOtpValid(anyString(), anyString());
    }

    // ========================
    // isCorrectScope()
    // ========================

    @Test
    @DisplayName("Deve retornar true quando scope for correto")
    void deveRetornarTrueQuandoScopeForCorreto() {
        // Given
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String scope = "OTP";
        when(usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope)).thenReturn(true);

        // When
        boolean resultado = usuarioService.isCorrectScope(httpRequest, scope);

        // Then
        assertTrue(resultado);
        verify(usuarioAutenticacaoAdapter, times(1)).isCorrectScope(httpRequest, scope);
    }

    @Test
    @DisplayName("Deve retornar false quando scope for incorreto")
    void deveRetornarFalseQuandoScopeForIncorreto() {
        // Given
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String scope = "INVALIDO";
        when(usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope)).thenReturn(false);

        // When
        boolean resultado = usuarioService.isCorrectScope(httpRequest, scope);

        // Then
        assertFalse(resultado);
    }

    // ========================
    // definirNovaSenha()
    // ========================

    @Test
    @DisplayName("Deve definir nova senha com sucesso")
    void deveDefinirNovaSenhaComSucesso() {
        // Given
        Usuario usuario = criarUsuarioAtivoComSenha();
        String senhaCriptografada = "senha-criptografada";
        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(EMAIL_TESTE);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(passwordEncoder.encode(SENHA_TESTE)).thenReturn(senhaCriptografada);

        // When
        usuarioService.definirNovaSenha(SENHA_TESTE);

        // Then
        assertEquals(senhaCriptografada, usuario.getSenha());
        verify(usuarioPersistenciaAdapter, times(1)).salvar(usuario);
        verify(otpService, times(1)).removeOtp(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir senha nula")
    void deveLancarExcecaoAoDefinirSenhaNula() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.definirNovaSenha(null)
        );

        verify(usuarioPersistenciaAdapter, never()).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao definir senha em branco")
    void deveLancarExcecaoAoDefinirSenhaEmBranco() {
        // When/Then
        assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.definirNovaSenha("   ")
        );

        verify(usuarioPersistenciaAdapter, never()).salvar(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve remover OTP ao definir nova senha")
    void deveRemoverOtpAoDefinirNovaSenha() {
        // Given
        Usuario usuario = criarUsuarioAtivoComSenha();
        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(EMAIL_TESTE);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(passwordEncoder.encode(SENHA_TESTE)).thenReturn("senha-criptografada");

        // When
        usuarioService.definirNovaSenha(SENHA_TESTE);

        // Then
        verify(otpService, times(1)).removeOtp(EMAIL_TESTE);
    }

    // ========================
    // autenticarOtp()
    // ========================

    @Test
    @DisplayName("Deve autenticar via OTP com sucesso")
    void deveAutenticarViaOtpComSucesso() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, OTP_TESTE);
        Usuario usuario = criarUsuarioAtivo();
        String token = "token-otp";
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);
        when(usuarioAutenticacaoAdapter.gerarTokenOtp(any(Authentication.class))).thenReturn(token);

        // When
        UsuarioTokenResponse resultado = usuarioService.autenticarOtp(request);

        // Then
        assertNotNull(resultado);
        verify(usuarioAutenticacaoAdapter, times(1)).definirContexto(any(Authentication.class));
        verify(usuarioAutenticacaoAdapter, times(1)).gerarTokenOtp(any(Authentication.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao autenticar via OTP com usuário inativo")
    void deveLancarExcecaoAoAutenticarViaOtpComUsuarioInativo() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_TESTE, OTP_TESTE);
        Usuario usuario = criarUsuarioInativo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_TESTE)).thenReturn(usuario);

        // When/Then
        assertThrows(
                UsuarioInativoException.class,
                () -> usuarioService.autenticarOtp(request)
        );

        verify(usuarioAutenticacaoAdapter, never()).gerarTokenOtp(any(Authentication.class));
    }

    // ========================
    // reativarUsuario()
    // ========================

    @Test
    @DisplayName("Deve reativar usuário inativo com sucesso")
    void deveReativarUsuarioInativoComSucesso() {
        // Given
        Usuario usuario = criarUsuarioInativo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(anyLong())).thenReturn(usuario);

        // When
        UsuarioListarResponse resultado = usuarioService.reativarUsuario(1L);

        // Then
        assertNotNull(resultado);
        assertTrue(usuario.isAtivo());
        verify(usuarioPersistenciaAdapter, times(1)).salvar(usuario);
    }

    @Test
    @DisplayName("Deve lançar exceção ao reativar usuário já ativo")
    void deveLancarExcecaoAoReativarUsuarioJaAtivo() {
        // Given
        Usuario usuario = criarUsuarioAtivo();
        when(usuarioPersistenciaAdapter.buscarUsuarioPorId(anyLong())).thenReturn(usuario);

        // When/Then
        assertThrows(
                UsuarioJaAtivoException.class,
                () -> usuarioService.reativarUsuario(1L)
        );

        verify(usuarioPersistenciaAdapter, never()).salvar(any(Usuario.class));
    }

    // ========================
    // Métodos auxiliares
    // ========================

    private Usuario criarUsuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(ID_TESTE);
        usuario.setNome(NOME_TESTE);
        usuario.setEmail(EMAIL_TESTE);
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());
        return usuario;
    }

    private Usuario criarUsuarioAtivoComSenha() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setSenha(SENHA_TESTE);
        return usuario;
    }

    private Usuario criarUsuarioAtivoSemSenha() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setSenha(null);
        return usuario;
    }

    private Usuario criarUsuarioInativo() {
        Usuario usuario = criarUsuarioAtivo();
        usuario.setAtivo(false);
        return usuario;
    }
}