package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.MicrosoftTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

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

    @InjectMocks
    private OneDriveTokenAdapter adapter;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(adapter, "clientId", "fake-client-id");
        ReflectionTestUtils.setField(adapter, "clientSecret", "fake-secret");
        ReflectionTestUtils.setField(adapter, "tenantId", "fake-tenant");
        ReflectionTestUtils.setField(adapter, "restTemplate", restTemplate);
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

        assertThrows(OneDriveErroException.class, () -> adapter.getAccessToken());
    }
}

