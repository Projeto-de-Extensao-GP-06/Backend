package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.exception.OneDriveErroException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OneDriveTokenAdapter")
class OneDriveTokenAdapterTest {

    @Mock
    private OneDriveTokenPort tokenPort;

    @Mock
    private OneDriveTokenCache tokenCache;

    @Mock
    private RestTemplate restTemplate;

    private OneDriveTokenAdapter tokenAdapter;

    private static final String TOKEN_URL = "https://login.microsoftonline.com/meu-tenant/oauth2/v2.0/token";

    @BeforeEach
    void setUp() {
        // Usamos um ObjectMapper real: é apenas parsing de JSON simples, sem configuração especial do Spring.
        tokenAdapter = new OneDriveTokenAdapter(tokenPort, tokenCache, restTemplate, new ObjectMapper());

        ReflectionTestUtils.setField(tokenAdapter, "clientId", "meu-client-id");
        ReflectionTestUtils.setField(tokenAdapter, "clientSecret", "meu-client-secret");
        ReflectionTestUtils.setField(tokenAdapter, "tenantId", "meu-tenant");
        ReflectionTestUtils.setField(tokenAdapter, "redirectUri", "http://localhost:8080/login/oauth2/code/azure");
    }

    @Nested
    @DisplayName("getAccessToken")
    class GetAccessTokenTest {

        @Test
        @DisplayName("Deve retornar o token em cache quando ainda estiver válido")
        void deveRetornarTokenDoCacheQuandoValido() {
            OneDriveTokenCache.CachedToken cached = new OneDriveTokenCache.CachedToken(
                    "token-em-cache", LocalDateTime.now().plusMinutes(30));
            when(tokenCache.get()).thenReturn(Optional.of(cached));

            String resultado = tokenAdapter.getAccessToken();

            assertEquals("token-em-cache", resultado);
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Deve renovar o token quando o cache estiver perto de expirar (margem de 5 min)")
        void deveRenovarQuandoCacheProximoDeExpirar() {
            OneDriveTokenCache.CachedToken cached = new OneDriveTokenCache.CachedToken(
                    "token-antigo", LocalDateTime.now().plusMinutes(2));
            when(tokenCache.get()).thenReturn(Optional.of(cached));

            MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-antigo", LocalDateTime.now());
            when(tokenPort.findTokenConfig()).thenReturn(Optional.of(config));
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(Map.of(
                            "access_token", "token-renovado",
                            "expires_in", "3600",
                            "refresh_token", "novo-refresh"
                    ));

            String resultado = tokenAdapter.getAccessToken();

            assertEquals("token-renovado", resultado);
            verify(tokenPort).save(config);
        }

        @Test
        @DisplayName("Deve renovar o token quando não houver nada em cache")
        void deveRenovarQuandoCacheVazio() {
            when(tokenCache.get()).thenReturn(Optional.empty());

            MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-antigo", LocalDateTime.now());
            when(tokenPort.findTokenConfig()).thenReturn(Optional.of(config));
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(Map.of(
                            "access_token", "token-novo",
                            "expires_in", "3600",
                            "refresh_token", "novo-refresh"
                    ));

            String resultado = tokenAdapter.getAccessToken();

            assertEquals("token-novo", resultado);
        }
    }

    @Nested
    @DisplayName("exchangeCodeForToken")
    class ExchangeCodeForTokenTest {

        @Test
        @DisplayName("Deve trocar o código por tokens e persistir o refresh token")
        void deveTrocarCodigoPorTokenComSucesso() {
            MicrosoftTokenConfig registro = new MicrosoftTokenConfig();
            when(tokenPort.createOrGetDefault()).thenReturn(registro);
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(Map.of(
                            "access_token", "token-abc",
                            "expires_in", "3600",
                            "refresh_token", "refresh-abc"
                    ));

            tokenAdapter.exchangeCodeForToken("codigo-de-autorizacao");

            assertEquals("refresh-abc", registro.getRefreshToken());
            verify(tokenPort).save(registro);

            ArgumentCaptor<OneDriveTokenCache.CachedToken> captor =
                    ArgumentCaptor.forClass(OneDriveTokenCache.CachedToken.class);
            verify(tokenCache).put(captor.capture());
            assertEquals("token-abc", captor.getValue().accessToken());
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando a Microsoft retornar corpo nulo")
        void deveLancarExcecaoQuandoRespostaNula() {
            when(tokenPort.createOrGetDefault()).thenReturn(new MicrosoftTokenConfig());
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(null);

            assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.exchangeCodeForToken("codigo"));
        }

        @Test
        @DisplayName("Deve lançar mensagem específica quando a Microsoft retornar invalid_grant")
        void deveLancarMensagemEspecificaParaInvalidGrant() {
            when(tokenPort.createOrGetDefault()).thenReturn(new MicrosoftTokenConfig());

            String corpoErro = "{\"error\":\"invalid_grant\",\"error_description\":\"código expirado\"}";
            HttpClientErrorException erroHttp = HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY,
                    corpoErro.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(erroHttp);

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.exchangeCodeForToken("codigo-expirado"));

            assertTrue(ex.getMessage().contains("Inicie o login novamente"));
        }

        @Test
        @DisplayName("Deve extrair a mensagem de erro da Microsoft para outros erros HTTP")
        void deveExtrairMensagemDeErroGenerica() {
            when(tokenPort.createOrGetDefault()).thenReturn(new MicrosoftTokenConfig());

            String corpoErro = "{\"error\":\"invalid_client\",\"error_description\":\"client secret inválido\"}";
            HttpClientErrorException erroHttp = HttpClientErrorException.create(
                    HttpStatus.UNAUTHORIZED, "Unauthorized", HttpHeaders.EMPTY,
                    corpoErro.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(erroHttp);

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.exchangeCodeForToken("codigo"));

            assertTrue(ex.getMessage().contains("client secret inválido"));
        }

        @Test
        @DisplayName("Deve envolver RestClientException genérica em OneDriveErroException")
        void deveEnvolverRestClientExceptionGenerica() {
            when(tokenPort.createOrGetDefault()).thenReturn(new MicrosoftTokenConfig());
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenThrow(new RestClientException("timeout de conexão"));

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.exchangeCodeForToken("codigo"));

            assertTrue(ex.getMessage().contains("timeout de conexão"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando a Microsoft não retornar refresh_token")
        void deveLancarExcecaoQuandoSemRefreshToken() {
            when(tokenPort.createOrGetDefault()).thenReturn(new MicrosoftTokenConfig());
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(Map.of("access_token", "token-abc", "expires_in", "3600"));

            assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.exchangeCodeForToken("codigo"));
            verify(tokenPort, never()).save(any());
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando não houver configuração de token salva")
        void deveLancarExcecaoQuandoNaoHouverConfigSalva() {
            when(tokenPort.findTokenConfig()).thenReturn(Optional.empty());

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> tokenAdapter.refreshToken());

            assertTrue(ex.getMessage().contains("/api-onedrive/auth/login"));
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("Deve renovar o access token usando o refresh token salvo")
        void deveRenovarTokenComSucesso() {
            MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-salvo", LocalDateTime.now());
            when(tokenPort.findTokenConfig()).thenReturn(Optional.of(config));
            when(restTemplate.postForObject(eq(TOKEN_URL), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(Map.of(
                            "access_token", "token-renovado",
                            "expires_in", "3600",
                            "refresh_token", "refresh-novo"
                    ));

            String resultado = tokenAdapter.refreshToken();

            assertEquals("token-renovado", resultado);
            assertEquals("refresh-novo", config.getRefreshToken());
            verify(tokenPort).save(config);
        }
    }
}
