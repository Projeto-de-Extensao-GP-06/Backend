package br.com.sgpj.controller;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteController - Testes Unitários")
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private ClienteCriacaoRequest clienteCriacaoRequest;

    @Mock
    private ClienteAtualizacaoRequest clienteAtualizacaoRequest;

    @Mock
    private ClienteDetalhesResponse clienteDetalhesResponse;

    @Mock
    private ClienteListarResponse clienteListarResponse;

    @InjectMocks
    private ClienteController clienteController;

    private static final Long CLIENTE_ID = 1L;

    // =========================================================================
    // criar
    // =========================================================================

    @Nested
    @DisplayName("POST /clientes - criar")
    class Criar {

        @Test
        @DisplayName("Deve criar cliente e retornar status 201 com Location header")
        void deveCriarClienteComStatus201ELocationHeader() {
            when(clienteDetalhesResponse.id()).thenReturn(CLIENTE_ID);
            when(clienteService.criar(clienteCriacaoRequest)).thenReturn(clienteDetalhesResponse);

            ResponseEntity<ClienteDetalhesResponse> resposta = clienteController.criar(clienteCriacaoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isEqualTo(clienteDetalhesResponse);
            assertThat(resposta.getHeaders().getLocation()).hasPath("/clientes/" + CLIENTE_ID);

            verify(clienteService).criar(clienteCriacaoRequest);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao criar cliente")
        void devePropagaExcecaoDoServiceAoCriarCliente() {
            when(clienteService.criar(clienteCriacaoRequest))
                    .thenThrow(new RuntimeException("Erro ao criar cliente"));

            assertThatThrownBy(() -> clienteController.criar(clienteCriacaoRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Erro ao criar cliente");
        }
    }

    // =========================================================================
    // atualizar
    // =========================================================================

    @Nested
    @DisplayName("PUT /clientes/{id} - atualizar")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar cliente e retornar status 200")
        void deveAtualizarClienteComStatus200() {
            when(clienteService.atualizar(CLIENTE_ID, clienteAtualizacaoRequest)).thenReturn(clienteDetalhesResponse);

            ResponseEntity<ClienteDetalhesResponse> resposta = clienteController.atualizar(CLIENTE_ID, clienteAtualizacaoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(clienteDetalhesResponse);

            verify(clienteService).atualizar(CLIENTE_ID, clienteAtualizacaoRequest);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao atualizar cliente")
        void devePropagaExcecaoDoServiceAoAtualizarCliente() {
            when(clienteService.atualizar(CLIENTE_ID, clienteAtualizacaoRequest))
                    .thenThrow(new ClienteNaoEncontradoException(CLIENTE_ID));

            assertThatThrownBy(() -> clienteController.atualizar(CLIENTE_ID, clienteAtualizacaoRequest))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessage("Cliente não encontrado para o ID: " + CLIENTE_ID);
        }
    }

    // =========================================================================
    // listarTodos
    // =========================================================================

    @Nested
    @DisplayName("GET /clientes - listarTodos")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista de clientes com status 200 quando há clientes cadastrados")
        void deveRetornarListaComStatus200QuandoHaClientes() {
            when(clienteService.listarTodos()).thenReturn(List.of(clienteListarResponse));

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(clienteService).listarTodos();
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há clientes cadastrados")
        void deveRetornarStatus204QuandoListaVazia() {
            when(clienteService.listarTodos()).thenReturn(List.of());

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();
        }
    }

    // =========================================================================
    // buscarPorId
    // =========================================================================

    @Nested
    @DisplayName("GET /clientes/{id} - buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar cliente com status 200")
        void deveRetornarClienteComStatus200() {
            when(clienteService.buscarPorId(CLIENTE_ID)).thenReturn(clienteDetalhesResponse);

            ResponseEntity<ClienteDetalhesResponse> resposta = clienteController.buscarPorId(CLIENTE_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(clienteDetalhesResponse);

            verify(clienteService).buscarPorId(CLIENTE_ID);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao buscar cliente por id")
        void devePropagaExcecaoDoServiceAoBuscarClientePorId() {
            when(clienteService.buscarPorId(CLIENTE_ID))
                    .thenThrow(new ClienteNaoEncontradoException(CLIENTE_ID));

            assertThatThrownBy(() -> clienteController.buscarPorId(CLIENTE_ID))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessage("Cliente não encontrado para o ID: " + CLIENTE_ID);
        }
    }

    // =========================================================================
    // listarPorStatus
    // =========================================================================

    @Nested
    @DisplayName("GET /clientes/status - listarPorStatus")
    class ListarPorStatus {

        private static final String STATUS = "ATIVO";

        @Test
        @DisplayName("Deve retornar lista de clientes com status 200 quando há clientes com o status informado")
        void deveRetornarListaComStatus200QuandoHaClientes() {
            when(clienteService.listarPorStatus(STATUS)).thenReturn(List.of(clienteListarResponse));

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.listarPorStatus(STATUS);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(clienteService).listarPorStatus(STATUS);
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há clientes com o status informado")
        void deveRetornarStatus204QuandoListaVazia() {
            when(clienteService.listarPorStatus(STATUS)).thenReturn(List.of());

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.listarPorStatus(STATUS);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();
        }
    }

    // =========================================================================
    // pesquisar
    // =========================================================================

    @Nested
    @DisplayName("GET /clientes/pesquisar - pesquisar")
    class Pesquisar {

        private static final String PESQUISA = "João";

        @Test
        @DisplayName("Deve retornar lista de clientes com status 200 quando há resultados para a pesquisa")
        void deveRetornarListaComStatus200QuandoHaResultados() {
            when(clienteService.pesquisarPorNomeOuCpfCnpj(PESQUISA)).thenReturn(List.of(clienteListarResponse));

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.pesquisar(PESQUISA);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).hasSize(1);

            verify(clienteService).pesquisarPorNomeOuCpfCnpj(PESQUISA);
        }

        @Test
        @DisplayName("Deve retornar status 204 quando não há resultados para a pesquisa")
        void deveRetornarStatus204QuandoSemResultados() {
            when(clienteService.pesquisarPorNomeOuCpfCnpj(PESQUISA)).thenReturn(List.of());

            ResponseEntity<List<ClienteListarResponse>> resposta = clienteController.pesquisar(PESQUISA);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();
        }
    }

    // =========================================================================
    // pesquisarComFiltros
    // =========================================================================

    @Nested
    @DisplayName("GET /clientes/filtrar - pesquisarComFiltros")
    class PesquisarComFiltros {

        @Test
        @DisplayName("Deve retornar página de clientes com status 200 e ordenação ascendente por padrão")
        void deveRetornarPaginaComStatus200EOrdenacaoAscendente() {
            String termo = "João";
            String status = "Ativo";
            String tipoPessoa = "PF";
            int page = 0;
            int size = 10;
            String sort = "nomeRazaoSocial,asc";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "nomeRazaoSocial"));
            Page<ClienteListarResponse> pagina = new PageImpl<>(List.of(clienteListarResponse), pageable, 1);

            when(clienteService.pesquisarComFiltros(termo, status, tipoPessoa, pageable)).thenReturn(pagina);

            ResponseEntity<Page<ClienteListarResponse>> resposta = clienteController.pesquisarComFiltros(
                    termo, status, tipoPessoa, page, size, sort);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getContent()).containsExactly(clienteListarResponse);

            verify(clienteService).pesquisarComFiltros(termo, status, tipoPessoa, pageable);
        }

        @Test
        @DisplayName("Deve retornar página de clientes com ordenação descendente quando especificada")
        void deveRetornarPaginaComOrdenacaoDescendente() {
            String termo = null;
            String status = "Todos";
            String tipoPessoa = "Todos";
            int page = 1;
            int size = 5;
            String sort = "nomeRazaoSocial,desc";

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "nomeRazaoSocial"));
            Page<ClienteListarResponse> pagina = new PageImpl<>(List.of(clienteListarResponse), pageable, 1);

            when(clienteService.pesquisarComFiltros(termo, status, tipoPessoa, pageable)).thenReturn(pagina);

            ResponseEntity<Page<ClienteListarResponse>> resposta = clienteController.pesquisarComFiltros(
                    termo, status, tipoPessoa, page, size, sort);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();

            verify(clienteService).pesquisarComFiltros(termo, status, tipoPessoa, pageable);
        }

        @Test
        @DisplayName("Deve usar direção ASC por padrão quando direção não for informada no sort")
        void deveUsarDirecaoAscQuandoDirecaoNaoInformada() {
            String sort = "cpfCnpj";
            Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "cpfCnpj"));
            Page<ClienteListarResponse> pagina = new PageImpl<>(List.of(clienteListarResponse), pageable, 1);

            when(clienteService.pesquisarComFiltros(null, "Todos", "Todos", pageable)).thenReturn(pagina);

            ResponseEntity<Page<ClienteListarResponse>> resposta = clienteController.pesquisarComFiltros(
                    null, "Todos", "Todos", 0, 10, sort);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(clienteService).pesquisarComFiltros(null, "Todos", "Todos", pageable);
        }
    }

    // =========================================================================
    // deletar
    // =========================================================================

    @Nested
    @DisplayName("DELETE /clientes/{id} - deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar cliente e retornar status 204")
        void deveDeletarClienteComStatus204() {
            doNothing().when(clienteService).deletar(CLIENTE_ID);

            ResponseEntity<Void> resposta = clienteController.deletar(CLIENTE_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(clienteService).deletar(CLIENTE_ID);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao deletar cliente")
        void devePropagaExcecaoDoServiceAoDeletarCliente() {
            doThrow(new ClienteNaoEncontradoException(CLIENTE_ID)).when(clienteService).deletar(CLIENTE_ID);

            assertThatThrownBy(() -> clienteController.deletar(CLIENTE_ID))
                    .isInstanceOf(ClienteNaoEncontradoException.class)
                    .hasMessage("Cliente não encontrado para o ID: " + CLIENTE_ID);
        }
    }
}