package br.com.sgpj.service;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.service.useCases.cliente.CreateCliente;
import br.com.sgpj.service.useCases.cliente.DeleteCliente;
import br.com.sgpj.service.useCases.cliente.ReadCliente;
import br.com.sgpj.service.useCases.cliente.UpdateCliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService")
class ClienteServiceTest {

    @Mock
    private CreateCliente createCliente;

    @Mock
    private ReadCliente readCliente;

    @Mock
    private UpdateCliente updateCliente;

    @Mock
    private DeleteCliente deleteCliente;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("criar deve delegar ao use case CreateCliente")
    void deveDelegarCriacaoAoUseCase() {
        ClienteCriacaoRequest request = mock(ClienteCriacaoRequest.class);
        ClienteDetalhesResponse resposta = mock(ClienteDetalhesResponse.class);
        when(createCliente.criarCliente(request)).thenReturn(resposta);

        ClienteDetalhesResponse resultado = clienteService.criar(request);

        assertEquals(resposta, resultado);
        verify(createCliente).criarCliente(request);
    }

    @Test
    @DisplayName("listarTodos deve delegar ao use case ReadCliente")
    void deveDelegarListagemAoUseCase() {
        List<ClienteListarResponse> lista = List.of(mock(ClienteListarResponse.class));
        when(readCliente.listarTodos()).thenReturn(lista);

        List<ClienteListarResponse> resultado = clienteService.listarTodos();

        assertEquals(lista, resultado);
        verify(readCliente).listarTodos();
    }

    @Test
    @DisplayName("buscarPorId deve delegar ao use case ReadCliente")
    void deveDelegarBuscaPorIdAoUseCase() {
        ClienteDetalhesResponse resposta = mock(ClienteDetalhesResponse.class);
        when(readCliente.buscarPorId(1L)).thenReturn(resposta);

        ClienteDetalhesResponse resultado = clienteService.buscarPorId(1L);

        assertEquals(resposta, resultado);
        verify(readCliente).buscarPorId(1L);
    }

    @Test
    @DisplayName("listarPorStatus deve delegar ao use case ReadCliente")
    void deveDelegarListagemPorStatusAoUseCase() {
        List<ClienteListarResponse> lista = List.of(mock(ClienteListarResponse.class));
        when(readCliente.listarPorStatus("ATIVO")).thenReturn(lista);

        List<ClienteListarResponse> resultado = clienteService.listarPorStatus("ATIVO");

        assertEquals(lista, resultado);
        verify(readCliente).listarPorStatus("ATIVO");
    }

    @Test
    @DisplayName("pesquisarPorNomeOuCpfCnpj deve remover espaços em branco antes de delegar")
    void deveRemoverEspacosAntesDePesquisar() {
        List<ClienteListarResponse> lista = List.of(mock(ClienteListarResponse.class));
        when(readCliente.pesquisarPorNomeOuCpfCnpj("Fulano")).thenReturn(lista);

        List<ClienteListarResponse> resultado = clienteService.pesquisarPorNomeOuCpfCnpj("  Fulano  ");

        assertEquals(lista, resultado);
        verify(readCliente).pesquisarPorNomeOuCpfCnpj("Fulano");
    }

    @Test
    @DisplayName("pesquisarComFiltros deve delegar ao use case ReadCliente com os mesmos filtros")
    void deveDelegarPesquisaComFiltrosAoUseCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ClienteListarResponse> pagina = new PageImpl<>(List.of(mock(ClienteListarResponse.class)));
        when(readCliente.pesquisarComFiltros("termo", "ATIVO", "PF", pageable)).thenReturn(pagina);

        Page<ClienteListarResponse> resultado = clienteService.pesquisarComFiltros("termo", "ATIVO", "PF", pageable);

        assertEquals(pagina, resultado);
        verify(readCliente).pesquisarComFiltros("termo", "ATIVO", "PF", pageable);
    }

    @Test
    @DisplayName("atualizar deve delegar ao use case UpdateCliente")
    void deveDelegarAtualizacaoAoUseCase() {
        ClienteAtualizacaoRequest request = mock(ClienteAtualizacaoRequest.class);
        ClienteDetalhesResponse resposta = mock(ClienteDetalhesResponse.class);
        when(updateCliente.atualizarCliente(1L, request)).thenReturn(resposta);

        ClienteDetalhesResponse resultado = clienteService.atualizar(1L, request);

        assertEquals(resposta, resultado);
        verify(updateCliente).atualizarCliente(1L, request);
    }

    @Test
    @DisplayName("deletar deve delegar ao use case DeleteCliente")
    void deveDelegarExclusaoAoUseCase() {
        clienteService.deletar(1L);

        verify(deleteCliente).deletarCliente(1L);
    }
}
