package br.com.sgpj.controller;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.EnviarOtpRequest;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.RefreshTokenResponse;
import br.com.sgpj.dto.response.UsuarioSessaoResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.security.AuthenticationCookieService;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.security.TokenBlacklistService;
import br.com.sgpj.service.RefreshTokenService;
import br.com.sgpj.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Testes Unitários")
class AuthControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private AuthenticationCookieService cookieService;

    @Mock
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthController authController;

    private static final long JWT_VALIDITY = 3600L;
    private static final long JWT_REFRESH_VALIDITY = 86400L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "jwtValidity", JWT_VALIDITY);
        ReflectionTestUtils.setField(authController, "jwtRefreshValidity", JWT_REFRESH_VALIDITY);
    }

    @Nested
    @DisplayName("me()")
    class MeTest {

        @Test
        @DisplayName("Deve retornar os dados do usuário autenticado")
        void deveRetornarSessaoDoUsuarioAutenticado() {
            Authentication authentication = mock(Authentication.class);
            UsuarioSessaoResponse sessao = new UsuarioSessaoResponse(
                    1L,
                    "Usuário Teste",
                    "usuario@teste.com",
                    new UsuarioSessaoResponse.CargoDto(1L, "Advogado"),
                    List.of("PROCESSO_CRIAR")
            );
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("usuario@teste.com");
            when(usuarioService.autenticar("usuario@teste.com")).thenReturn(sessao);

            ResponseEntity<UsuarioSessaoResponse> response = authController.me(authentication);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(sessao);
            verify(usuarioService).autenticar("usuario@teste.com");
        }

        @Test
        @DisplayName("Deve retornar 401 sem autenticação válida")
        void deveRetornar401SemAutenticacao() {
            ResponseEntity<UsuarioSessaoResponse> response = authController.me(null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(usuarioService);
        }
    }

    // =========================================================================
    // login
    // =========================================================================

    @Nested
    @DisplayName("login()")
    class LoginTest {

        @Test
        @DisplayName("Deve realizar login com sucesso, definir cookies e retornar sessão do usuário")
        void deveRealizarLoginComSucesso() {
            // Given
            UsuarioLoginRequest request = new UsuarioLoginRequest("usuario@teste.com", "senha123");
            UsuarioSessaoResponse.CargoDto cargoDto = new UsuarioSessaoResponse.CargoDto(1L, "Advogado");
            UsuarioTokenResponse autenticado = new UsuarioTokenResponse(
                    1L,
                    "Usuário Teste",
                    "usuario@teste.com",
                    "access-token-jwt",
                    "refresh-token-uuid",
                    cargoDto,
                    List.of("PROCESSO_CRIAR")
            );

            when(usuarioService.autenticar(request)).thenReturn(autenticado);

            // When
            ResponseEntity<UsuarioSessaoResponse> response = authController.login(request, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            assertThat(response.getBody().nome()).isEqualTo("Usuário Teste");
            assertThat(response.getBody().email()).isEqualTo("usuario@teste.com");

            verify(rateLimiter).checkLoginRateLimit(anyString());
            verify(usuarioService).autenticar(request);

            // Verifica adição do cookie de access token
            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("access-token-jwt"),
                    eq(Duration.ofSeconds(JWT_VALIDITY)),
                    eq("authToken"),
                    eq("/")
            );

            // Verifica adição do cookie de refresh token
            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("refresh-token-uuid"),
                    eq(Duration.ofSeconds(JWT_REFRESH_VALIDITY)),
                    eq("refreshToken"),
                    eq("/auth")
            );
        }
    }

    // =========================================================================
    // logout
    // =========================================================================

    @Nested
    @DisplayName("logout()")
    class LogoutTest {

        @Test
        @DisplayName("Deve realizar logout, invalidar token JWT e refresh token quando presentes, e remover cookies")
        void deveRealizarLogoutComSucessoQuandoTokensPresentes() {
            // Given
            String refreshToken = "refresh-token-uuid";
            String jwtToken = "jwt-access-token";
            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(jwtToken);

            // When
            ResponseEntity<Void> response = authController.logout(refreshToken, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            verify(tokenBlacklistService).blacklistToken(jwtToken);
            verify(refreshTokenService).revogarToken(refreshToken);
            verify(cookieService).remover(httpResponse, "authToken", "/");
            verify(cookieService).remover(httpResponse, "refreshToken", "/auth");
        }

        @Test
        @DisplayName("Deve realizar logout com sucesso quando tokens não forem encontrados na requisição")
        void deveRealizarLogoutQuandoTokensNulosOuEmBranco() {
            // Given
            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(null);

            // When
            ResponseEntity<Void> response = authController.logout("   ", httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            verify(tokenBlacklistService, never()).blacklistToken(anyString());
            verify(refreshTokenService, never()).revogarToken(anyString());
            verify(cookieService).remover(httpResponse, "authToken", "/");
            verify(cookieService).remover(httpResponse, "refreshToken", "/auth");
        }

        @Test
        @DisplayName("Deve realizar logout com sucesso quando refreshToken for null")
        void deveRealizarLogoutQuandoRefreshTokenForNull() {
            // Given
            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(null);

            // When
            ResponseEntity<Void> response = authController.logout(null, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

            verify(tokenBlacklistService, never()).blacklistToken(anyString());
            verify(refreshTokenService, never()).revogarToken(anyString());
            verify(cookieService).remover(httpResponse, "authToken", "/");
            verify(cookieService).remover(httpResponse, "refreshToken", "/auth");
        }
    }

    // =========================================================================
    // refreshToken
    // =========================================================================

    @Nested
    @DisplayName("refreshToken()")
    class RefreshTokenTest {

        @Test
        @DisplayName("Deve retornar 401 Unauthorized quando refreshToken for nulo")
        void deveRetornar401QuandoRefreshTokenNulo() {
            // When
            ResponseEntity<Void> response = authController.refreshToken(null, httpResponse, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(refreshTokenService);
            verifyNoInteractions(cookieService);
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized quando refreshToken for em branco")
        void deveRetornar401QuandoRefreshTokenEmBranco() {
            // When
            ResponseEntity<Void> response = authController.refreshToken("   ", httpResponse, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verifyNoInteractions(refreshTokenService);
            verifyNoInteractions(cookieService);
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName("Deve rotacionar tokens com sucesso e adicionar novos cookies com token presente na requisição")
        void deveRotacionarTokensComSucessoQuandoTokenPresenteNaRequisicao() {
            // Given
            String refreshToken = "refresh-token-antigo";
            String tokenRequisicao = "jwt-antigo";
            RefreshTokenResponse novosTokens = new RefreshTokenResponse("novo-jwt", "novo-refresh");

            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(tokenRequisicao);
            when(refreshTokenService.validarERotacionar(refreshToken)).thenReturn(novosTokens);

            // When
            ResponseEntity<Void> response = authController.refreshToken(refreshToken, httpResponse, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(tokenBlacklistService).blacklistToken(tokenRequisicao);
            verify(refreshTokenService).validarERotacionar(refreshToken);

            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("novo-jwt"),
                    eq(Duration.ofSeconds(JWT_VALIDITY)),
                    eq("authToken"),
                    eq("/")
            );

            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("novo-refresh"),
                    eq(Duration.ofSeconds(JWT_REFRESH_VALIDITY)),
                    eq("refreshToken"),
                    eq("/auth")
            );
        }

        @Test
        @DisplayName("Deve rotacionar tokens com sucesso quando não houver token no header da requisição")
        void deveRotacionarTokensComSucessoQuandoTokenNaoPresenteNaRequisicao() {
            // Given
            String refreshToken = "refresh-token-antigo";
            RefreshTokenResponse novosTokens = new RefreshTokenResponse("novo-jwt", "novo-refresh");

            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(null);
            when(refreshTokenService.validarERotacionar(refreshToken)).thenReturn(novosTokens);

            // When
            ResponseEntity<Void> response = authController.refreshToken(refreshToken, httpResponse, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(tokenBlacklistService, never()).blacklistToken(anyString());
            verify(refreshTokenService).validarERotacionar(refreshToken);

            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("novo-jwt"),
                    eq(Duration.ofSeconds(JWT_VALIDITY)),
                    eq("authToken"),
                    eq("/")
            );

            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("novo-refresh"),
                    eq(Duration.ofSeconds(JWT_REFRESH_VALIDITY)),
                    eq("refreshToken"),
                    eq("/auth")
            );
        }
    }

    // =========================================================================
    // enviarRedefinirSenhaOtp
    // =========================================================================

    @Nested
    @DisplayName("enviarRedefinirSenhaOtp()")
    class EnviarRedefinirSenhaOtpTest {

        @Test
        @DisplayName("Deve enviar OTP para redefinição de senha com verificação de rate limit")
        void deveEnviarOtpRedefinirSenhaComSucesso() {
            // Given
            EnviarOtpRequest request = new EnviarOtpRequest("usuario@teste.com");

            // When
            ResponseEntity<String> response = authController.enviarRedefinirSenhaOtp(request, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Se o e-mail estiver cadastrado, um código será enviado em instantes.");

            verify(rateLimiter).checkOtpRateLimit(anyString());
            verify(usuarioService).enviarRedefinirSenhaOtp("usuario@teste.com");
        }
    }

    // =========================================================================
    // enviarDefinirSenhaOtp
    // =========================================================================

    @Nested
    @DisplayName("enviarDefinirSenhaOtp()")
    class EnviarDefinirSenhaOtpTest {

        @Test
        @DisplayName("Deve enviar OTP para definição de senha inicial com verificação de rate limit")
        void deveEnviarOtpDefinirSenhaComSucesso() {
            // Given
            EnviarOtpRequest request = new EnviarOtpRequest("novo@teste.com");

            // When
            ResponseEntity<String> response = authController.enviarDefinirSenhaOtp(request, httpRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Se o e-mail estiver cadastrado, um código será enviado em instantes.");

            verify(rateLimiter).checkOtpRateLimit(anyString());
            verify(usuarioService).enviarDefinirSenhaOtp("novo@teste.com");
        }
    }

    // =========================================================================
    // validarOtp
    // =========================================================================

    @Nested
    @DisplayName("validarOtp()")
    class ValidarOtpTest {

        @Test
        @DisplayName("Deve validar OTP, adicionar cookie temporário de autenticação e retornar sucesso")
        void deveValidarOtpComSucesso() {
            // Given
            ValidarOtpRequest request = new ValidarOtpRequest("usuario@teste.com", "123456");
            UsuarioTokenResponse autenticado = new UsuarioTokenResponse(
                    1L,
                    "Usuário",
                    "usuario@teste.com",
                    "temp-token-jwt",
                    null,
                    null,
                    null
            );

            when(usuarioService.autenticarOtp(request)).thenReturn(autenticado);

            // When
            ResponseEntity<String> response = authController.validarOtp(request, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Código validado com sucesso!");

            verify(rateLimiter).checkOtpRateLimit(anyString());
            verify(usuarioService).autenticarOtp(request);
            verify(cookieService).adicionar(
                    eq(httpResponse),
                    eq("temp-token-jwt"),
                    eq(Duration.ofMinutes(15)),
                    eq("authToken"),
                    eq("/auth")
            );
        }
    }

    // =========================================================================
    // definirNovaSenha
    // =========================================================================

    @Nested
    @DisplayName("definirNovaSenha()")
    class DefinirNovaSenhaTest {

        @Test
        @DisplayName("Deve definir nova senha, invalidar token atual na blacklist e remover cookie de autenticação")
        void deveDefinirNovaSenhaComSucessoQuandoTokenPresente() {
            // Given
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("novaSenhaForte123");
            String token = "jwt-temp-token";
            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(token);

            // When
            ResponseEntity<String> response = authController.definirNovaSenha(request, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Nova senha definida com sucesso!");

            verify(usuarioService).definirNovaSenha(request);
            verify(tokenBlacklistService).blacklistToken(token);
            verify(cookieService).remover(httpResponse, "authToken", "/auth");
        }

        @Test
        @DisplayName("Deve definir nova senha com sucesso quando token não estiver presente na requisição")
        void deveDefinirNovaSenhaComSucessoQuandoTokenNaoPresente() {
            // Given
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("novaSenhaForte123");
            when(gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest)).thenReturn(null);

            // When
            ResponseEntity<String> response = authController.definirNovaSenha(request, httpRequest, httpResponse);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("Nova senha definida com sucesso!");

            verify(usuarioService).definirNovaSenha(request);
            verify(tokenBlacklistService, never()).blacklistToken(anyString());
            verify(cookieService).remover(httpResponse, "authToken", "/auth");
        }
    }
}
