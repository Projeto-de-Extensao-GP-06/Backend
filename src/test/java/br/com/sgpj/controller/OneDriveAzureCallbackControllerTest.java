package br.com.sgpj.controller;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAzureCallbackController - Testes Unitários")
class OneDriveAzureCallbackControllerTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    @InjectMocks
    private OneDriveAzureCallbackController controller;

    @Nested
    @DisplayName("callbackLegado")
    class CallbackLegadoTest {

        @Test
        @DisplayName("Deve trocar o code por tokens e retornar sucesso")
        void deveProcessarCallbackComSucesso() {
            ResponseEntity<Map<String, String>> resposta = controller.callbackLegado("codigo-valido", null, null);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody().get("status")).isEqualTo("connected");
            verify(tokenAdapter).exchangeCodeForToken("codigo-valido");
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando a Microsoft retornar um erro")
        void deveLancarExcecaoQuandoMicrosoftRetornaErro() {
            assertThatThrownBy(() -> controller.callbackLegado(null, "access_denied", "O usuário negou o acesso"))
                    .isInstanceOf(OneDriveErroException.class)
                    .hasMessageContaining("access_denied")
                    .hasMessageContaining("O usuário negou o acesso");

            verifyNoInteractions(tokenAdapter);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o code estiver ausente")
        void deveLancarExcecaoQuandoCodeAusente() {
            assertThatThrownBy(() -> controller.callbackLegado(null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("code");

            verifyNoInteractions(tokenAdapter);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando o code estiver em branco")
        void deveLancarExcecaoQuandoCodeEmBranco() {
            assertThatThrownBy(() -> controller.callbackLegado("   ", null, null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(tokenAdapter);
        }

        @Test
        @DisplayName("Deve decodificar a mensagem de erro vinda com codificação de URL")
        void deveDecodificarMensagemDeErroComUrlEncoding() {
            assertThatThrownBy(() -> controller.callbackLegado(null, "access_denied", "usu%C3%A1rio+cancelou"))
                    .isInstanceOf(OneDriveErroException.class)
                    .hasMessageContaining("usuário cancelou");
        }
    }
}
