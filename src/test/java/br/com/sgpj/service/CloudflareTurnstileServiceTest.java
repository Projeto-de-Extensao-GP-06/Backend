package br.com.sgpj.service;

import br.com.sgpj.dto.response.TurnstileVerifyResponse;
import br.com.sgpj.exception.CaptchaInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CloudflareTurnstileService")
class CloudflareTurnstileServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CloudflareTurnstileService service;
    private final String secretKey = "test-secret-key";

    @BeforeEach
    void setUp() {
        service = new CloudflareTurnstileService(restTemplate, secretKey);
    }

    @Test
    @DisplayName("validarToken deve passar quando Cloudflare retornar success true")
    void validarTokenDevePassarQuandoSucesso() {
        TurnstileVerifyResponse response = new TurnstileVerifyResponse(true, "2026-10-08T18:00:00Z", "localhost", null, null, null);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(TurnstileVerifyResponse.class)))
                .thenReturn(response);

        assertDoesNotThrow(() -> service.validarToken("valid-token", "127.0.0.1"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq("https://challenges.cloudflare.com/turnstile/v0/siteverify"), captor.capture(), eq(TurnstileVerifyResponse.class));

        MultiValueMap<String, String> body = captor.getValue().getBody();
        assertEquals(secretKey, body.getFirst("secret"));
        assertEquals("valid-token", body.getFirst("response"));
        assertEquals("127.0.0.1", body.getFirst("remoteip"));
    }

    @Test
    @DisplayName("validarToken deve lançar exceção se token for nulo ou vazio")
    void validarTokenDeveLancarExcecaoQuandoTokenNuloOuVazio() {
        assertThrows(CaptchaInvalidoException.class, () -> service.validarToken(null, "127.0.0.1"));
        assertThrows(CaptchaInvalidoException.class, () -> service.validarToken("  ", "127.0.0.1"));
    }

    @Test
    @DisplayName("validarToken deve lançar exceção quando Cloudflare retornar success false")
    void validarTokenDeveLancarExcecaoQuandoCloudflareFalhar() {
        TurnstileVerifyResponse response = new TurnstileVerifyResponse(false, null, null, List.of("invalid-input-response"), null, null);
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(TurnstileVerifyResponse.class)))
                .thenReturn(response);

        assertThrows(CaptchaInvalidoException.class, () -> service.validarToken("invalid-token", "127.0.0.1"));
    }

    @Test
    @DisplayName("validarToken deve lançar exceção quando resposta for nula")
    void validarTokenDeveLancarExcecaoQuandoRespostaNula() {
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(TurnstileVerifyResponse.class)))
                .thenReturn(null);

        assertThrows(CaptchaInvalidoException.class, () -> service.validarToken("token", "127.0.0.1"));
    }

    @Test
    @DisplayName("validarToken deve lançar exceção quando RestTemplate lançar erro de comunicação")
    void validarTokenDeveLancarExcecaoQuandoErroComunicacao() {
        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(TurnstileVerifyResponse.class)))
                .thenThrow(new RestClientException("Connection timed out"));

        assertThrows(CaptchaInvalidoException.class, () -> service.validarToken("token", "127.0.0.1"));
    }
}
