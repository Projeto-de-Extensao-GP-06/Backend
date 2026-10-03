package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.service.OneDriveReconexaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAuthController - Testes Unitários")
class OneDriveAuthControllerTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    @Mock
    private OneDriveReconexaoService reconexaoService;

    private OneDriveAuthController controller;

    private static final String CLIENT_ID = "meu-client-id";
    private static final String TENANT_ID = "meu-tenant";
    private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/azure";
    private static final String FRONTEND_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        controller = new OneDriveAuthController(tokenAdapter, reconexaoService);
        ReflectionTestUtils.setField(controller, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(controller, "tenantId", TENANT_ID);
        ReflectionTestUtils.setField(controller, "redirectUri", REDIRECT_URI);
        ReflectionTestUtils.setField(controller, "frontendUrl", FRONTEND_URL);
    }

    @Nested
    @DisplayName("iniciarLogin")
    class IniciarLoginTest {

        @Test
        @DisplayName("Deve retornar a URL de autorização da Microsoft com os parâmetros corretos")
        void deveRetornarUrlDeAutorizacao() {
            ResponseEntity<Map<String, String>> resposta = controller.iniciarLogin();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            String authUrl = resposta.getBody().get("authUrl");
            assertThat(authUrl).startsWith("https://login.microsoftonline.com/" + TENANT_ID + "/oauth2/v2.0/authorize");
            assertThat(authUrl).contains("client_id=" + CLIENT_ID);
            assertThat(authUrl).contains("response_type=code");
            assertThat(resposta.getBody().get("redirectUri")).isEqualTo(REDIRECT_URI);
        }
    }

    @Nested
    @DisplayName("redirecionarParaLogin")
    class RedirecionarParaLoginTest {

        @Test
        @DisplayName("Deve responder com 302 e Location apontando para a URL de login da Microsoft")
        void deveRedirecionarParaMicrosoft() {
            ResponseEntity<Void> resposta = controller.redirecionarParaLogin();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(resposta.getHeaders().getLocation().toString())
                    .startsWith("https://login.microsoftonline.com/" + TENANT_ID + "/oauth2/v2.0/authorize");
        }
    }

    @Nested
    @DisplayName("callback")
    class CallbackTest {

        @Test
        @DisplayName("Deve trocar o code por tokens e redirecionar para o frontend com sucesso")
        void deveProcessarCallbackComSucesso() {
            when(reconexaoService.concluir("codigo-valido", null, null)).thenReturn(true);

            ResponseEntity<Void> resposta = controller.callback("codigo-valido", null, null);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(resposta.getHeaders().getLocation().toString())
                    .isEqualTo("http://localhost:5173/app/perfil?onedrive=connected");
            verify(reconexaoService).concluir("codigo-valido", null, null);
        }

        @Test
        @DisplayName("Deve redirecionar para o frontend com erro quando a Microsoft retornar erro")
        void deveRedirecionarComErro() {
            when(reconexaoService.concluir(null, "access_denied", "O usuário negou o acesso")).thenReturn(false);

            ResponseEntity<Void> resposta = controller.callback(null, "access_denied", "O usuário negou o acesso");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(resposta.getHeaders().getLocation().toString())
                    .isEqualTo("http://localhost:5173/app/perfil?onedrive=error");
            verify(reconexaoService).concluir(null, "access_denied", "O usuário negou o acesso");
        }
    }
}
