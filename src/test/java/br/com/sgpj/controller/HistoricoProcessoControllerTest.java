package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.service.HistoricoProcessoService;
import br.com.sgpj.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoricoProcessoController - Testes Unitários")
class HistoricoProcessoControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private HistoricoProcessoService historicoProcessoService;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HistoricoProcessoRequest historicoProcessoRequest;

    @Mock
    private HistoricoProcessoResponse historicoProcessoResponse;

    @InjectMocks
    private HistoricoProcessoController historicoProcessoController;

    private static final Long PROCESSO_ID = 1L;
    private static final String SCOPE_GENERAL_USE = "GENERAL_USE";

    // =========================================================================
    // interacaoProcesso
    // =========================================================================

    @Nested
    @DisplayName("POST /historico - interacaoProcesso")
    class InteracaoProcesso {

        @Test
        @DisplayName("Deve registrar interação e retornar status 201 quando escopo é válido")
        void deveRegistrarInteracaoComStatus201QuandoEscopoValido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(true);
            when(historicoProcessoService.adicionarRegistro(historicoProcessoRequest)).thenReturn(historicoProcessoResponse);

            ResponseEntity<HistoricoProcessoResponse> resposta = historicoProcessoController
                    .interacaoProcesso(historicoProcessoRequest, httpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isEqualTo(historicoProcessoResponse);

            verify(usuarioService).isCorrectScope(httpRequest, SCOPE_GENERAL_USE);
            verify(historicoProcessoService).adicionarRegistro(historicoProcessoRequest);
        }

        @Test
        @DisplayName("Deve lançar EscopoJwtException quando escopo é inválido ao registrar interação")
        void deveLancarEscopoJwtExceptionQuandoEscopoInvalido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(false);

            assertThatThrownBy(() -> historicoProcessoController.interacaoProcesso(historicoProcessoRequest, httpRequest))
                    .isInstanceOf(EscopoJwtException.class)
                    .hasMessageContaining("Acesso negado")
                    .hasMessageContaining("criação de histórico");

            verify(historicoProcessoService, never()).adicionarRegistro(any());
        }
    }

    // =========================================================================
    // buscarPorIdProcesso
    // =========================================================================

    @Nested
    @DisplayName("GET /processo/{id}/historico - buscarPorIdProcesso")
    class BuscarPorIdProcesso {

        @Test
        @DisplayName("Deve retornar histórico do processo com status 200 quando escopo é válido")
        void deveRetornarHistoricoComStatus200QuandoEscopoValido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(true);
            when(historicoProcessoService.buscarPorIdProcesso(PROCESSO_ID)).thenReturn(List.of(historicoProcessoResponse));

            ResponseEntity<List<HistoricoProcessoResponse>> resposta = historicoProcessoController
                    .buscarPorIdProcesso(PROCESSO_ID, httpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(historicoProcessoService).buscarPorIdProcesso(PROCESSO_ID);
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 quando processo não possui histórico")
        void deveRetornarListaVaziaComStatus200QuandoSemHistorico() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(true);
            when(historicoProcessoService.buscarPorIdProcesso(PROCESSO_ID)).thenReturn(List.of());

            ResponseEntity<List<HistoricoProcessoResponse>> resposta = historicoProcessoController
                    .buscarPorIdProcesso(PROCESSO_ID, httpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar EscopoJwtException quando escopo é inválido ao buscar histórico")
        void deveLancarEscopoJwtExceptionQuandoEscopoInvalido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(false);

            assertThatThrownBy(() -> historicoProcessoController.buscarPorIdProcesso(PROCESSO_ID, httpRequest))
                    .isInstanceOf(EscopoJwtException.class)
                    .hasMessageContaining("Acesso negado")
                    .hasMessageContaining("visualização de histórico");

            verify(historicoProcessoService, never()).buscarPorIdProcesso(any());
        }
    }

    // =========================================================================
    // deletarPorIdProcesso
    // =========================================================================

    @Nested
    @DisplayName("DELETE /historico - deletarPorIdProcesso")
    class DeletarPorIdProcesso {

        @Test
        @DisplayName("Deve deletar histórico e retornar status 204 quando escopo é válido")
        void deveDeletarHistoricoComStatus204QuandoEscopoValido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(true);
            doNothing().when(historicoProcessoService).deletarPorIdProcesso(PROCESSO_ID);

            ResponseEntity<Void> resposta = historicoProcessoController.deletarPorIdProcesso(PROCESSO_ID, httpRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(usuarioService).isCorrectScope(httpRequest, SCOPE_GENERAL_USE);
            verify(historicoProcessoService).deletarPorIdProcesso(PROCESSO_ID);
        }

        @Test
        @DisplayName("Deve lançar EscopoJwtException quando escopo é inválido ao deletar histórico")
        void deveLancarEscopoJwtExceptionQuandoEscopoInvalido() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(false);

            assertThatThrownBy(() -> historicoProcessoController.deletarPorIdProcesso(PROCESSO_ID, httpRequest))
                    .isInstanceOf(EscopoJwtException.class)
                    .hasMessageContaining("Acesso negado")
                    .hasMessageContaining("deleção de histórico");

            verify(historicoProcessoService, never()).deletarPorIdProcesso(any());
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao deletar histórico")
        void devePropagaExcecaoDoServiceAoDeletarHistorico() {
            when(usuarioService.isCorrectScope(httpRequest, SCOPE_GENERAL_USE)).thenReturn(true);
            doThrow(new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + PROCESSO_ID)).when(historicoProcessoService).deletarPorIdProcesso(PROCESSO_ID);

            assertThatThrownBy(() -> historicoProcessoController.deletarPorIdProcesso(PROCESSO_ID, httpRequest))
                    .isInstanceOf(ObjetoNaoEncontradoException.class)
                    .hasMessage("Processo não encontrado para o ID: " + PROCESSO_ID);
        }
    }
}