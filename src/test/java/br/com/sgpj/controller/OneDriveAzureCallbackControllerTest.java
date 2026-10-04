package br.com.sgpj.controller;

import br.com.sgpj.service.OneDriveReconexaoService;
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
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveAzureCallbackController - Testes Unitários")
class OneDriveAzureCallbackControllerTest {

    @Mock
    private OneDriveReconexaoService reconexaoService;

    @InjectMocks
    private OneDriveAzureCallbackController controller;

    private static final String FRONTEND_URL = "http://localhost:5173";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "frontendUrl", FRONTEND_URL);
    }

    @Nested
    @DisplayName("callbackLegado")
    class CallbackLegadoTest {

        @Test
        @DisplayName("Deve redirecionar para o frontend com sucesso quando a conexão for concluída")
        void deveRedirecionarComSucesso() {
            when(reconexaoService.concluir("codigo-valido", null, null)).thenReturn(true);

            ResponseEntity<Void> resposta = controller.callbackLegado("codigo-valido", null, null);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(resposta.getHeaders().getLocation().toString())
                    .isEqualTo("http://localhost:5173/app/perfil?onedrive=connected");
            verify(reconexaoService).concluir("codigo-valido", null, null);
        }

        @Test
        @DisplayName("Deve redirecionar para o frontend com status error quando a conexão falhar")
        void deveRedirecionarComErro() {
            when(reconexaoService.concluir(null, "access_denied", "O usuário negou o acesso")).thenReturn(false);

            ResponseEntity<Void> resposta = controller.callbackLegado(null, "access_denied", "O usuário negou o acesso");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.FOUND);
            assertThat(resposta.getHeaders().getLocation().toString())
                    .isEqualTo("http://localhost:5173/app/perfil?onedrive=error");
            verify(reconexaoService).concluir(null, "access_denied", "O usuário negou o acesso");
        }
    }
}
