package br.com.sgpj.controller;

import br.com.sgpj.dto.request.*;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.dto.response.UsuarioSessaoResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Testes Unitários")
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private UsuarioCriacaoRequest usuarioCriacaoRequest;

    @Mock
    private UsuarioLoginRequest usuarioLoginRequest;

    @Mock
    private UsuarioAtualizacaoRequest usuarioAtualizacaoRequest;

    @Mock
    private UsuarioListarResponse usuarioListarResponse;

    @Mock
    private UsuarioSessaoResponse usuarioSessaoResponse;

    @Mock
    private UsuarioTokenResponse usuarioTokenResponse;

    @Mock
    private EnviarOtpRequest enviarOtpRequest;

    @Mock
    private ValidarOtpRequest validarOtpRequest;

    @Mock
    private DefinirNovaSenhaRequest definirNovaSenhaRequest;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private UsuarioController usuarioController;

    private static final Long USUARIO_ID = 1L;
    private static final String EMAIL = "usuario@teste.com";
    private static final String TOKEN = "jwt-token-mock";

    // =========================================================================
    // criar
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios - criar")
    class Criar {

        @Test
        @DisplayName("Deve criar usuário e retornar status 201")
        void deveCriarUsuarioComStatus201() {
            ResponseEntity<Void> resposta = usuarioController.criar(usuarioCriacaoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isNull();

            verify(usuarioService).criar(usuarioCriacaoRequest);
        }
    }

    // =========================================================================
    // login
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/login - login")
    class Login {

        @Test
        @DisplayName("Deve autenticar usuário, adicionar cookie e retornar sessão com status 200")
        void deveAutenticarERetornarSessaoComStatus200() {
            ReflectionTestUtils.setField(usuarioController, "jwtValidity", 3600L);

            when(usuarioTokenResponse.token()).thenReturn(TOKEN);
            when(usuarioTokenResponse.id()).thenReturn(USUARIO_ID);

            try (MockedStatic<UsuarioMapper> mapperMock = mockStatic(UsuarioMapper.class)) {
                mapperMock.when(() -> UsuarioMapper.toEntity(usuarioLoginRequest)).thenReturn(usuario);
                mapperMock.when(() -> UsuarioMapper.toSessaoDto(usuarioTokenResponse, usuario)).thenReturn(usuarioSessaoResponse);

                when(usuarioService.autenticar(usuario)).thenReturn(usuarioTokenResponse);
                when(usuarioService.obterPorId(USUARIO_ID)).thenReturn(usuario);

                ResponseEntity<UsuarioSessaoResponse> resposta = usuarioController.login(usuarioLoginRequest, httpResponse);

                assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(resposta.getBody()).isEqualTo(usuarioSessaoResponse);

                verify(usuarioService).autenticar(usuario);
                verify(usuarioService).obterPorId(USUARIO_ID);
                verify(httpResponse).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
            }
        }

        @Test
        @DisplayName("Deve adicionar cookie com o token JWT retornado na autenticação")
        void deveAdicionarCookieComTokenJwt() {
            ReflectionTestUtils.setField(usuarioController, "jwtValidity", 3600L);

            when(usuarioTokenResponse.token()).thenReturn(TOKEN);
            when(usuarioTokenResponse.id()).thenReturn(USUARIO_ID);

            try (MockedStatic<UsuarioMapper> mapperMock = mockStatic(UsuarioMapper.class)) {
                mapperMock.when(() -> UsuarioMapper.toEntity(usuarioLoginRequest)).thenReturn(usuario);
                mapperMock.when(() -> UsuarioMapper.toSessaoDto(any(), any())).thenReturn(usuarioSessaoResponse);

                when(usuarioService.autenticar(usuario)).thenReturn(usuarioTokenResponse);
                when(usuarioService.obterPorId(USUARIO_ID)).thenReturn(usuario);

                usuarioController.login(usuarioLoginRequest, httpResponse);

                verify(httpResponse).addHeader(eq(HttpHeaders.SET_COOKIE), contains(TOKEN));
            }
        }
    }

    // =========================================================================
    // logout
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/logout - logout")
    class Logout {

        @Test
        @DisplayName("Deve retornar status 204 e adicionar cookie expirado ao fazer logout")
        void deveRetornarStatus204EExpirarCookieAoFazerLogout() {
            ResponseEntity<Void> resposta = usuarioController.logout(httpResponse);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(httpResponse).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
        }
    }

    // =========================================================================
    // listarTodos
    // =========================================================================

    @Nested
    @DisplayName("GET /usuarios - listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista de usuários com status 200 quando há usuários cadastrados")
        void deveRetornarListaComStatus200QuandoHaUsuarios() {
            when(usuarioService.listarTodos()).thenReturn(List.of(usuarioListarResponse));

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(usuarioService).listarTodos();
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há usuários cadastrados")
        void deveRetornarStatus204QuandoListaVazia() {
            when(usuarioService.listarTodos()).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    // =========================================================================
    // listarPorFuncao
    // =========================================================================

    @Nested
    @DisplayName("GET /usuarios/funcao - listarPorFuncao")
    class ListarPorFuncao {

        private static final String FUNCAO = "ANALISTA";

        @Test
        @DisplayName("Deve retornar lista com status 200 quando há usuários com a função informada")
        void deveRetornarListaComStatus200QuandoHaUsuarios() {
            when(usuarioService.listarPorFuncao(FUNCAO)).thenReturn(List.of(usuarioListarResponse));

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarPorFuncao(FUNCAO);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(usuarioService).listarPorFuncao(FUNCAO);
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há usuários com a função informada")
        void deveRetornarStatus204QuandoListaVazia() {
            when(usuarioService.listarPorFuncao(FUNCAO)).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarPorFuncao(FUNCAO);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    // =========================================================================
    // pesquisarPorNomeOuEmail
    // =========================================================================

    @Nested
    @DisplayName("GET /usuarios/pesquisar - pesquisarPorNomeOuEmail")
    class PesquisarPorNomeOuEmail {

        private static final String PESQUISA = "João";

        @Test
        @DisplayName("Deve retornar lista com status 200 quando há resultados para a pesquisa")
        void deveRetornarListaComStatus200QuandoHaResultados() {
            when(usuarioService.buscarPorNomeOuEmail(PESQUISA)).thenReturn(List.of(usuarioListarResponse));

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.pesquisarPorNomeOuEmail(PESQUISA);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(usuarioService).buscarPorNomeOuEmail(PESQUISA);
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há resultados para a pesquisa")
        void deveRetornarStatus204QuandoSemResultados() {
            when(usuarioService.buscarPorNomeOuEmail(PESQUISA)).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.pesquisarPorNomeOuEmail(PESQUISA);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    // =========================================================================
    // atualizarUsuario
    // =========================================================================

    @Nested
    @DisplayName("PUT /usuarios/{id} - atualizarUsuario")
    class AtualizarUsuario {

        @Test
        @DisplayName("Deve atualizar usuário e retornar status 200")
        void deveAtualizarUsuarioComStatus200() {
            when(usuarioService.atualizarUsuario(usuarioAtualizacaoRequest, USUARIO_ID)).thenReturn(usuarioListarResponse);

            ResponseEntity<UsuarioListarResponse> resposta = usuarioController.atualizarUsuario(usuarioAtualizacaoRequest, USUARIO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(usuarioListarResponse);

            verify(usuarioService).atualizarUsuario(usuarioAtualizacaoRequest, USUARIO_ID);
        }
    }

    // =========================================================================
    // deletar
    // =========================================================================

    @Nested
    @DisplayName("DELETE /usuarios/{id} - deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar usuário e retornar status 204 quando id é diferente do logado")
        void deveDeletarUsuarioComStatus204QuandoIdDiferente() {
            when(usuarioService.isDifferentId(USUARIO_ID)).thenReturn(true);

            ResponseEntity<Void> resposta = usuarioController.deletar(USUARIO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(usuarioService).deletar(USUARIO_ID);
        }

        @Test
        @DisplayName("Deve lançar UsuarioAutoExclusaoException quando usuário tenta deletar a si mesmo")
        void deveLancarUsuarioAutoExclusaoExceptionQuandoMesmoId() {
            when(usuarioService.isDifferentId(USUARIO_ID)).thenReturn(false);

            assertThatThrownBy(() -> usuarioController.deletar(USUARIO_ID))
                    .isInstanceOf(UsuarioAutoExclusaoException.class)
                    .hasMessageContaining("próprio usuário");

            verify(usuarioService, never()).deletar(any());
        }
    }

    // =========================================================================
    // enviarRedefinirSenhaOtp
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/enviar-redefinir-senha-otp - enviarRedefinirSenhaOtp")
    class EnviarRedefinirSenhaOtp {

        @Test
        @DisplayName("Deve enviar OTP de redefinição e retornar status 200 com mensagem genérica")
        void deveEnviarOtpRedefinicaoERetornarStatus200() {
            when(enviarOtpRequest.email()).thenReturn(EMAIL);

            ResponseEntity<String> resposta = usuarioController.enviarRedefinirSenhaOtp(enviarOtpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).contains("código");

            verify(usuarioService).enviarRedefinirSenhaOtp(EMAIL);
        }
    }

    // =========================================================================
    // enviarDefinirSenhaOtp
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/enviar-definir-senha-otp - enviarDefinirSenhaOtp")
    class EnviarDefinirSenhaOtp {

        @Test
        @DisplayName("Deve enviar OTP de definição e retornar status 200 com mensagem genérica")
        void deveEnviarOtpDefinicaoERetornarStatus200() {
            when(enviarOtpRequest.email()).thenReturn(EMAIL);

            ResponseEntity<String> resposta = usuarioController.enviarDefinirSenhaOtp(enviarOtpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).contains("código");

            verify(usuarioService).enviarDefinirSenhaOtp(EMAIL);
        }
    }

    // =========================================================================
    // validarOtp
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/validar-otp - validarOtp")
    class ValidarOtp {

        @Test
        @DisplayName("Deve validar OTP, adicionar cookie e retornar status 200 quando código é válido")
        void deveValidarOtpERetornarStatus200QuandoCodigoValido() {
            when(usuarioService.validarOtp(validarOtpRequest)).thenReturn(true);
            when(usuarioService.autenticarOtp(validarOtpRequest)).thenReturn(usuarioTokenResponse);
            when(usuarioTokenResponse.token()).thenReturn(TOKEN);

            ResponseEntity<String> resposta = usuarioController.validateOtp(validarOtpRequest, httpResponse);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).contains("sucesso");

            verify(httpResponse).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
        }

        @Test
        @DisplayName("Deve retornar status 400 quando código OTP é inválido ou expirado")
        void deveRetornarStatus400QuandoCodigoOtpInvalido() {
            when(usuarioService.validarOtp(validarOtpRequest)).thenReturn(false);

            ResponseEntity<String> resposta = usuarioController.validateOtp(validarOtpRequest, httpResponse);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(resposta.getBody()).contains("inválido");

            verify(usuarioService, never()).autenticarOtp(any());
            verify(httpResponse, never()).addHeader(any(), any());
        }
    }

    // =========================================================================
    // definirNovaSenha
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/definir-nova-senha - definirNovaSenha")
    class DefinirNovaSenha {

        private static final String NOVA_SENHA = "novaSenha@123";

        @Test
        @DisplayName("Deve definir nova senha, expirar cookie e retornar status 200")
        void deveDefinirNovaSenhaERetornarStatus200() {
            when(definirNovaSenhaRequest.senha()).thenReturn(NOVA_SENHA);

            ResponseEntity<String> resposta = usuarioController.definirNovaSenha(definirNovaSenhaRequest, httpResponse);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).contains("sucesso");

            verify(usuarioService).definirNovaSenha(NOVA_SENHA);
            verify(httpResponse).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Max-Age=0"));
        }
    }

    // =========================================================================
    // reativarUsuario
    // =========================================================================

    @Nested
    @DisplayName("POST /usuarios/reactivate/{id} - reativarUsuario")
    class ReativarUsuario {

        @Test
        @DisplayName("Deve reativar usuário e retornar status 200")
        void deveReativarUsuarioComStatus200() {
            when(usuarioService.reativarUsuario(USUARIO_ID)).thenReturn(usuarioListarResponse);

            ResponseEntity<UsuarioListarResponse> resposta = usuarioController.reativarUsuario(USUARIO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(usuarioListarResponse);

            verify(usuarioService).reativarUsuario(USUARIO_ID);
        }
    }
}