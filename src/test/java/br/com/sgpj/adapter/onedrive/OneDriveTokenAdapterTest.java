package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.MicrosoftTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveTokenAdapter — Testes do adapter de tokens")
class OneDriveTokenAdapterTest {

    @Mock
    private MicrosoftTokenRepository tokenRepository;

    @Mock
    private RestTemplate restTemplate;

    private OneDriveTokenAdapter adapter;

    @BeforeEach
    void setup() {
        adapter = new OneDriveTokenAdapter(tokenRepository, restTemplate);
        ReflectionTestUtils.setField(adapter, "clientId", "fake-client-id");
        ReflectionTestUtils.setField(adapter, "clientSecret", "fake-secret");
        ReflectionTestUtils.setField(adapter, "tenantId", "fake-tenant");
        ReflectionTestUtils.setField(adapter, "redirectUri", "http://localhost:8080/login/oauth2/code/azure");
    }

    @Test
    @DisplayName("Deve retornar token em cache quando ainda não expirou")
    void deveRetornarTokenEmCache() {
        // Simula que já existe um token válido em cache
        ReflectionTestUtils.setField(adapter, "accessToken", "token-cache");
        ReflectionTestUtils.setField(adapter, "expiresAt", LocalDateTime.now().plusHours(1));

        String resultado = adapter.getAccessToken();

        assertEquals("token-cache", resultado);
        verifyNoInteractions(tokenRepository); // Não deve acessar o banco
    }

    @Test
    @DisplayName("Deve renovar token quando expirado")
    void deveRenovarQuandoExpirado() {
        ReflectionTestUtils.setField(adapter, "accessToken", "token-antigo");
        ReflectionTestUtils.setField(adapter, "expiresAt", LocalDateTime.now().minusMinutes(1));

        MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-antigo", LocalDateTime.now());
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(config));

        Map<String, Object> resposta = Map.of(
                "access_token", "token-novo",
                "refresh_token", "refresh-novo",
                "expires_in", "3600"
        );
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(resposta);

        String resultado = adapter.getAccessToken();

        assertEquals("token-novo", resultado);
        assertEquals("refresh-novo", config.getRefreshToken());
        verify(tokenRepository).save(config);
    }

    @Test
    @DisplayName("Deve renovar token quando cache está vazio (primeira chamada)")
    void deveRenovarQuandoCacheVazio() {
        MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-1", LocalDateTime.now());
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(config));

        Map<String, Object> resposta = Map.of(
                "access_token", "primeiro-token",
                "refresh_token", "refresh-2",
                "expires_in", "3600"
        );
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(resposta);

        String resultado = adapter.getAccessToken();

        assertEquals("primeiro-token", resultado);
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token não existe no banco")
    void deveLancarExcecaoSemRefreshToken() {
        when(tokenRepository.findById(1L)).thenReturn(Optional.empty());

        OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                () -> adapter.getAccessToken());

        assertTrue(ex.getMessage().contains("Refresh token não encontrado"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando resposta da Microsoft é nula")
    void deveLancarExcecaoRespostaNula() {
        MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh", LocalDateTime.now());
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(config));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(null);

        OneDriveErroException ex = assertThrows(OneDriveErroException.class, () -> adapter.getAccessToken());
        assertTrue(ex.getMessage().contains("Resposta nula"));
    }

    @Test
    @DisplayName("Deve trocar authorization code por refresh token e persistir registro")
    void deveTrocarCodigoPorToken() {
        when(tokenRepository.findById(1L)).thenReturn(Optional.empty());
        Map<String, Object> resposta = Map.of(
                "access_token", "access-token-novo",
                "refresh_token", "refresh-token-novo",
                "expires_in", "3600"
        );
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenReturn(resposta);

        adapter.trocarCodigoPorToken("authorization-code");

        verify(tokenRepository).save(argThat(config ->
                config.getId().equals(1L)
                        && "refresh-token-novo".equals(config.getRefreshToken())
                        && config.getDataCriacao() != null
        ));
    }

    @Test
    @DisplayName("Deve orientar reconexao quando refresh token estiver invalido")
    void deveOrientarReconexaoQuandoRefreshTokenInvalido() {
        MicrosoftTokenConfig config = new MicrosoftTokenConfig(1L, "refresh-expirado", LocalDateTime.now());
        when(tokenRepository.findById(1L)).thenReturn(Optional.of(config));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenThrow(httpClientErrorInvalidGrant());

        OneDriveErroException ex = assertThrows(OneDriveErroException.class, () -> adapter.getAccessToken());

        assertTrue(ex.getMessage().contains("Reconecte o OneDrive"));
    }

    @Test
    @DisplayName("Deve pedir novo login quando authorization code estiver invalido")
    void devePedirNovoLoginQuandoAuthorizationCodeInvalido() {
        when(tokenRepository.findById(1L)).thenReturn(Optional.empty());
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class))).thenThrow(httpClientErrorInvalidGrant());

        OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                () -> adapter.trocarCodigoPorToken("authorization-code"));

        assertTrue(ex.getMessage().contains("Inicie o login novamente"));
    }

    private HttpClientErrorException httpClientErrorInvalidGrant() {
        String body = "{\"error\":\"invalid_grant\",\"error_description\":\"simulado\"}";
        return HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
    }
}
