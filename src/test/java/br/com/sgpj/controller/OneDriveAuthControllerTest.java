package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAuthController — Testes do fluxo de reconexao")
class OneDriveAuthControllerTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    @InjectMocks
    private OneDriveAuthController controller;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(controller, "clientId", "client-id");
        ReflectionTestUtils.setField(controller, "tenantId", "common");
        ReflectionTestUtils.setField(controller, "redirectUri", "http://localhost:8080/login/oauth2/code/azure");
    }

    @Test
    @DisplayName("GET /login — Deve retornar a URL da Microsoft")
    void iniciarLoginDeveRetornarUrl() {
        ResponseEntity<Map<String, String>> resposta = controller.iniciarLogin();
        Map<String, String> body = Objects.requireNonNull(resposta.getBody());

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertTrue(Objects.requireNonNull(body.get("authUrl")).contains("login.microsoftonline.com/common/oauth2/v2.0/authorize"));
        assertTrue(Objects.requireNonNull(body.get("authUrl")).contains("client_id=client-id"));
        assertTrue(Objects.requireNonNull(body.get("authUrl")).contains("scope=offline_access"));
        assertEquals("http://localhost:8080/login/oauth2/code/azure", body.get("redirectUri"));
    }

    @Test
    @DisplayName("GET /login/redirect — Deve redirecionar para a Microsoft")
    void redirecionarParaLoginDeveRetornar302() {
        ResponseEntity<Void> resposta = controller.redirecionarParaLogin();

        assertEquals(HttpStatus.FOUND, resposta.getStatusCode());
        assertTrue(Objects.requireNonNull(resposta.getHeaders().getLocation()).toString().contains("login.microsoftonline.com/common/oauth2/v2.0/authorize"));
    }

    @Test
    @DisplayName("GET /callback — Deve trocar o codigo por token")
    void callbackDeveTrocarCodigoPorToken() {
        doNothing().when(tokenAdapter).trocarCodigoPorToken("codigo-ok");

        ResponseEntity<Map<String, String>> resposta = controller.callback("codigo-ok", null, null);
        Map<String, String> body = Objects.requireNonNull(resposta.getBody());

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("connected", body.get("status"));
        verify(tokenAdapter).trocarCodigoPorToken("codigo-ok");
    }

    @Test
    @DisplayName("GET /callback — Deve falhar sem code")
    void callbackDeveFalharSemCode() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.callback(null, null, null));

        assertTrue(ex.getMessage().contains("code"));
    }

    @Test
    @DisplayName("GET /callback — Deve propagar erro da Microsoft")
    void callbackDevePropagarErroDaMicrosoft() {
        OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                () -> controller.callback(null, "access_denied", "user+canceled"));

        assertTrue(ex.getMessage().contains("access_denied"));
    }
}
