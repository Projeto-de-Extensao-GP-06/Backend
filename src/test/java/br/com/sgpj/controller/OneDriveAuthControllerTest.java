package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAuthController - Testes Unitários")
class OneDriveAuthControllerTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    private OneDriveAuthController controller;

    private static final String CLIENT_ID = "meu-client-id";
    private static final String TENANT_ID = "meu-tenant";
    private static final String REDIRECT_URI = "http://localhost:8080/login/oauth2/code/azure";

    @BeforeEach
    void setUp() {
        controller = new OneDriveAuthController(tokenAdapter);
        ReflectionTestUtils.setField(controller, "clientId", CLIENT_ID);
        ReflectionTestUtils.setField(controller, "tenantId", TENANT_ID);
        ReflectionTestUtils.setField(controller, "redirectUri", REDIRECT_URI);
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
        @DisplayName("Deve trocar o code por tokens e retornar sucesso")
        void deveProcessarCallbackComSucesso() {
            ResponseEntity<Map<String, String>> resposta = controller.callback("codigo-valido", null, null);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("status")).isEqualTo("connected");
            verify(tokenAdapter).exchangeCodeForToken("codigo-valido");
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando a Microsoft retornar um erro")
        void deveLancarExcecaoQuandoMicrosoftRetornaErro() {
            assertThatThrownBy(() -> controller.callback(null, "access_denied", "O usuário negou o acesso"))
                    .isInstanceOf(OneDriveErroException.class)
                    .hasMessageContaining("access_denied")
                    .hasMessageContaining("O usuário negou o acesso");

            verifyNoInteractions(tokenAdapter);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o code estiver ausente")
        void deveLancarExcecaoQuandoCodeAusente() {
            assertThatThrownBy(() -> controller.callback(null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("code");

            verifyNoInteractions(tokenAdapter);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o code estiver em branco")
        void deveLancarExcecaoQuandoCodeEmBranco() {
            assertThatThrownBy(() -> controller.callback("   ", null, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(tokenAdapter);
        }
    }
}
