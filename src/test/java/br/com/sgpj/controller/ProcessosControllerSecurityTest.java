package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.ProcessoService;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessosController - Security Tests")
class ProcessosControllerSecurityTest {

    @Mock
    private ProcessoService processoService;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private Usuario usuario;

    @Mock
    private ProcessoAppCriacaoRequest processoAppCriacaoRequest;

    @Mock
    private ProcessoAppCreateResponse processoAppCreateResponse;

    @InjectMocks
    private ProcessosController controller;

    @Nested
    @DisplayName("POST /processos - novoProcesso")
    class NovoProcessoSecurityTest {

        @Test
        @DisplayName("Deve obter usuario autenticado antes de criar processo")
        void deveObterUsuarioAutenticadoAntesDeCriarProcesso() {
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
            when(processoService.criarProcesso(processoAppCriacaoRequest, usuario)).thenReturn(processoAppCreateResponse);

            ResponseEntity<ProcessoAppCreateResponse> resposta = controller.novoProcesso(processoAppCriacaoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(authorizationUtil).getUsuarioLogado();
            verify(processoService).criarProcesso(processoAppCriacaoRequest, usuario);
        }

        @Test
        @DisplayName("Deve propagar excecao quando usuario nao for encontrado")
        void devePropagarExcecaoQuandoUsuarioNaoForEncontrado() {
            when(authorizationUtil.getUsuarioLogado()).thenThrow(new AcessoNegadoException("Usuário não encontrado"));

            assertThatThrownBy(() -> controller.novoProcesso(processoAppCriacaoRequest))
                    .isInstanceOf(AcessoNegadoException.class)
                    .hasMessage("Usuário não encontrado");

            verify(authorizationUtil).getUsuarioLogado();
            verifyNoInteractions(processoService);
        }

        @Test
        @DisplayName("Deve propagar excecao quando acesso for negado")
        void devePropagarExcecaoQuandoAcessoForNegado() {
            when(authorizationUtil.getUsuarioLogado()).thenThrow(new AcessoNegadoException("Acesso negado"));

            assertThatThrownBy(() -> controller.novoProcesso(processoAppCriacaoRequest))
                    .isInstanceOf(AcessoNegadoException.class)
                    .hasMessage("Acesso negado");

            verify(authorizationUtil).getUsuarioLogado();
            verifyNoInteractions(processoService);
        }

        @Test
        @DisplayName("Deve garantir que usuario autenticado e sempre usado")
        void deveGarantirQueUsuarioAutenticadoESempreUsado() {
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
            when(processoService.criarProcesso(processoAppCriacaoRequest, usuario)).thenReturn(processoAppCreateResponse);

            controller.novoProcesso(processoAppCriacaoRequest);

            verify(authorizationUtil, times(1)).getUsuarioLogado();
            verify(processoService, times(1)).criarProcesso(processoAppCriacaoRequest, usuario);
        }
    }
}
