package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAzureCallbackController — Testes do callback legado")
class OneDriveAzureCallbackControllerTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    @InjectMocks
    private OneDriveAzureCallbackController controller;

    @Test
    @DisplayName("GET /login/oauth2/code/azure — Deve trocar o codigo por token")
    void callbackLegadoDeveTrocarCodigoPorToken() {
        doNothing().when(tokenAdapter).trocarCodigoPorToken("codigo-ok");

        ResponseEntity<Map<String, String>> resposta = controller.callbackLegado("codigo-ok", null, null);
        Map<String, String> body = Objects.requireNonNull(resposta.getBody());

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("connected", body.get("status"));
        verify(tokenAdapter).trocarCodigoPorToken("codigo-ok");
    }

    @Test
    @DisplayName("GET /login/oauth2/code/azure — Deve propagar erro da Microsoft")
    void callbackLegadoDevePropagarErroDaMicrosoft() {
        OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                () -> controller.callbackLegado(null, "access_denied", "user+canceled"));

        assertTrue(ex.getMessage().contains("access_denied"));
    }
}
