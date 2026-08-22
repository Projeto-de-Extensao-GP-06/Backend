package br.com.sgpj.controller;

import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.ProcessoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.*;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessosController — Endpoints de processo_documento")
class ProcessosControllerTest {

    @Mock private ProcessoService processoService;
    @Mock private AuthorizationUtil authorizationUtil;
    @Mock private ProcessoAppCriacaoRequest processoAppCriacaoRequest;
    @Mock private ProcessoAppUpdateRequest processoAppUpdateRequest;
    @Mock private ProcessoAppCreateResponse processoAppCreateResponse;
    @Mock private ProcessoAppUpdateResponse processoAppUpdateResponse;
    @Mock private ProcessoAppResponse processoAppResponse;
    @Mock private ProcessoAppMiniResponse processoAppMiniResponse;
    @Mock private Usuario usuario;
    @InjectMocks private ProcessosController controller;

    private ProcessoDocumentoResponse docResponse;

    private static final Long PROCESSO_ID = 1L;
    private static final Long CLIENTE_ID = 10L;

    @BeforeEach
    void setup() {
        docResponse = new ProcessoDocumentoResponse(
                20L, "contrato.pdf", "application/pdf",
                "https://download.url", 1L, "Usuario Responsavel",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                "RG", 5L, "rafael-silva-12345678901/001"
        );
    }

    // ========== POST /processos ==========

    @Nested
    @DisplayName("POST /processos - novoProcesso")
    class NovoProcesso {

        @Test
        @DisplayName("Deve criar processo e retornar status 201 quando escopo é válido")
        void deveCriarProcessoComStatus201QuandoEscopoValido() {
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
            when(processoService.criarProcesso(processoAppCriacaoRequest, usuario)).thenReturn(processoAppCreateResponse);

            ResponseEntity<ProcessoAppCreateResponse> resposta = controller.novoProcesso(processoAppCriacaoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isEqualTo(processoAppCreateResponse);

            verify(authorizationUtil).getUsuarioLogado();
            verify(processoService).criarProcesso(processoAppCriacaoRequest, usuario);
        }
    }

    // ========== PUT /processos/{id} ==========

    @Nested
    @DisplayName("PUT /processos/{id} - atualizarProcesso")
    class AtualizarProcesso {

        @Test
        @DisplayName("Deve atualizar processo e retornar status 200 quando escopo é válido")
        void deveAtualizarProcessoComStatus200QuandoEscopoValido() {
            when(processoService.atualizarProcesso(PROCESSO_ID, processoAppUpdateRequest)).thenReturn(processoAppUpdateResponse);

            ResponseEntity<ProcessoAppUpdateResponse> resposta = controller.atualizarProcesso(
                    processoAppUpdateRequest, PROCESSO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(processoAppUpdateResponse);

            verify(processoService).atualizarProcesso(PROCESSO_ID, processoAppUpdateRequest);
        }
    }

    // ========== DELETE /processos/{id} ==========

    @Nested
    @DisplayName("DELETE /processos/{id} - deletarProcesso")
    class DeletarProcesso {

        @Test
        @DisplayName("Deve deletar processo e retornar status 204 quando escopo é válido")
        void deveDeletarProcessoComStatus204QuandoEscopoValido() {
            doNothing().when(processoService).deletarProcesso(PROCESSO_ID);

            ResponseEntity<Void> resposta = controller.deletarProcesso(PROCESSO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(processoService).deletarProcesso(PROCESSO_ID);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao deletar processo")
        void devePropagaExcecaoDoServiceAoDeletarProcesso() {
            doThrow(new ObjetoNaoEncontradoException("Processo não encontrado")).when(processoService).deletarProcesso(PROCESSO_ID);

            assertThatThrownBy(() -> controller.deletarProcesso(PROCESSO_ID))
                    .isInstanceOf(ObjetoNaoEncontradoException.class)
                    .hasMessage("Processo não encontrado");
        }
    }

    // ========== GET /processos ==========

    @Nested
    @DisplayName("GET /processos - listarProcessos")
    class ListarProcessos {

        @Test
        @DisplayName("Deve retornar lista de processos com status 200 quando escopo é válido")
        void deveRetornarListaComStatus200QuandoEscopoValido() {
            when(processoService.listarProcessos()).thenReturn(List.of(processoAppMiniResponse));

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.listarProcessos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(processoService).listarProcessos();
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 quando não há processos")
        void deveRetornarListaVaziaComStatus200QuandoNaoHaProcessos() {
            when(processoService.listarProcessos()).thenReturn(List.of());

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.listarProcessos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEmpty();
        }
    }

    // ========== GET /processos/{id} ==========

    @Nested
    @DisplayName("GET /processos/{id} - buscarProcessoPorId")
    class BuscarProcessoPorId {

        @Test
        @DisplayName("Deve retornar processo com status 200 quando escopo é válido")
        void deveRetornarProcessoComStatus200QuandoEscopoValido() {
            when(processoService.buscarPorId(PROCESSO_ID)).thenReturn(processoAppResponse);

            ResponseEntity<ProcessoAppResponse> resposta = controller.buscarProcessoPorId(PROCESSO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(processoAppResponse);

            verify(processoService).buscarPorId(PROCESSO_ID);
        }
    }

    // ========== GET /clientes/{clienteId}/processos ==========

    @Nested
    @DisplayName("GET /clientes/{clienteId}/processos - buscarPorClienteId")
    class BuscarPorClienteId {

        @Test
        @DisplayName("Deve retornar lista de processos do cliente com status 200 quando escopo é válido")
        void deveRetornarProcessosDoClienteComStatus200QuandoEscopoValido() {
            when(processoService.buscarPorClienteId(CLIENTE_ID)).thenReturn(List.of(processoAppMiniResponse));

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.buscarPorClienteId(CLIENTE_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(processoService).buscarPorClienteId(CLIENTE_ID);
        }

        @Test
        @DisplayName("Deve retornar lista vazia com status 200 quando cliente não possui processos")
        void deveRetornarListaVaziaComStatus200QuandoClienteSemProcessos() {
            when(processoService.buscarPorClienteId(CLIENTE_ID)).thenReturn(List.of());

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.buscarPorClienteId(CLIENTE_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEmpty();
        }
    }

    // ========== GET /processos/buscaFiltro ==========

    @Nested
    @DisplayName("GET /processos/buscaFiltro - buscaFiltro")
    class BuscaFiltro {

        private static final String QUERY = "contrato";
        private static final Long FASE_ID = 2L;
        private static final Long TRIBUNAL_ID = 3L;

        @Test
        @DisplayName("Deve retornar lista filtrada com status 200 quando todos os parâmetros são informados")
        void deveRetornarListaFiltradaComTodosOsParametros() {
            when(processoService.buscaFiltro(QUERY, FASE_ID, TRIBUNAL_ID)).thenReturn(List.of(processoAppMiniResponse));

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.buscaFiltro(
                    QUERY, FASE_ID, TRIBUNAL_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);
            assertThat(resposta.getBody().getFirst()).isEqualTo(processoAppMiniResponse);
            assertThat(resposta.getBody()).hasSize(1);

            verify(processoService).buscaFiltro(QUERY, FASE_ID, TRIBUNAL_ID);
        }

        @Test
        @DisplayName("Deve retornar lista com status 200 quando parâmetros opcionais são nulos")
        void deveRetornarListaComStatus200QuandoParametrosOpcionaisNulos() {
            when(processoService.buscaFiltro(null, null, null)).thenReturn(List.of(processoAppMiniResponse));

            ResponseEntity<List<ProcessoAppMiniResponse>> resposta = controller.buscaFiltro(
                    null, null, null);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);
            assertThat(resposta.getBody().getFirst()).isEqualTo(processoAppMiniResponse);

            verify(processoService).buscaFiltro(null, null, null);
        }
    }

    // ========== POST /processos/{id}/documentos ==========

    @Test
    @DisplayName("POST /processos/{id}/documentos — Deve retornar 201 ao anexar documento")
    void anexarDocumentoDeveRetornar201() {
        when(processoService.anexarDocumento(10L, 20L, 1L)).thenReturn(docResponse);

        ResponseEntity<ProcessoDocumentoResponse> resp = controller.anexarDocumento(10L, 20L, 1L);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(20L, resp.getBody().documentoId());
        assertEquals("contrato.pdf", resp.getBody().nomeOriginal());
        verify(processoService).anexarDocumento(10L, 20L, 1L);
    }

    @Test
    @DisplayName("POST /processos/{id}/documentos — Deve repassar processoId do path ao service")
    void anexarDocumentoDeveRepassarProcessoId() {
        when(processoService.anexarDocumento(42L, 7L, 3L)).thenReturn(docResponse);

        ResponseEntity<ProcessoDocumentoResponse> resp = controller.anexarDocumento(42L, 7L, 3L);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        verify(processoService).anexarDocumento(42L, 7L, 3L);
    }

    // ========== GET /processos/{id}/documentos ==========

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar 200 com lista de documentos")
    void listarDocumentosDeveRetornar200() {
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(List.of(docResponse));

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.getBody().size());
        assertEquals(20L, resp.getBody().getFirst().documentoId());
        assertEquals("contrato.pdf", resp.getBody().getFirst().nomeOriginal());
    }

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar 204 quando processo sem documentos")
    void listarDocumentosDeveRetornar204() {
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        assertNull(resp.getBody());
    }

    @Test
    @DisplayName("GET /processos/{id}/documentos — Deve retornar múltiplos documentos")
    void listarDocumentosDeveRetornarMultiplos() {
        ProcessoDocumentoResponse doc2 = new ProcessoDocumentoResponse(
                21L, "procuracao.pdf", "application/pdf",
                "https://download2.url", 2L, "Outro Usuario",
                LocalDateTime.of(2026, 2, 1, 9, 0),
                "Procuração", 5L, "rafael-silva-12345678901/001"
        );
        when(processoService.listarDocumentosDoProcesso(10L)).thenReturn(List.of(docResponse, doc2));

        ResponseEntity<List<ProcessoDocumentoResponse>> resp = controller.listarDocumentosDoProcesso(10L);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(2, resp.getBody().size());
        assertEquals(20L, resp.getBody().get(0).documentoId());
        assertEquals(21L, resp.getBody().get(1).documentoId());
    }
}

