package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.security.AuthorizationUtil;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadCliente Use Case Testes")
class ReadClienteTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @InjectMocks
    private ReadCliente readCliente;

    private Cliente criarCliente(Long id, String nome, String statusNome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNomeRazaoSocial(nome);
        cliente.setCpfCnpj("12345678901");
        cliente.setPF(true);
        cliente.setStatusCliente(new StatusCliente(1L, statusNome));
        return cliente;
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTest {

        @Test
        @DisplayName("Deve buscar todos os clientes usando findAll por padrão")
        void deveBuscarTodosOsClientesUsandoFindAll() {
            Cliente ativo = criarCliente(1L, "Cliente Ativo", "ATIVO");
            Cliente lead = criarCliente(2L, "Cliente Lead", "LEAD");

            when(clienteRepository.findAll()).thenReturn(List.of(ativo, lead));

            List<ClienteListarResponse> resultado = readCliente.listarTodos();

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals("Cliente Ativo", resultado.get(0).nomeRazaoSocial());
            assertEquals("Cliente Lead", resultado.get(1).nomeRazaoSocial());
            verify(clienteRepository).findAll();
            verify(clienteRepository, never()).findByStatusCliente_NomeNotIgnoreCase(any());
        }

        @Test
        @DisplayName("Deve buscar todos os clientes quando excluirLead for falso")
        void deveBuscarTodosOsClientesQuandoExcluirLeadFalso() {
            Cliente ativo = criarCliente(1L, "Cliente Ativo", "ATIVO");
            when(clienteRepository.findAll()).thenReturn(List.of(ativo));

            List<ClienteListarResponse> resultado = readCliente.listarTodos(false);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(clienteRepository).findAll();
        }

        @Test
        @DisplayName("Deve buscar clientes excluindo status LEAD quando excluirLead for verdadeiro")
        void deveBuscarClientesExcluindoStatusLeadQuandoSolicitado() {
            Cliente ativo = criarCliente(1L, "Cliente Ativo", "ATIVO");
            Cliente inativo = criarCliente(2L, "Cliente Inativo", "INATIVO");

            when(clienteRepository.findByStatusCliente_NomeNotIgnoreCase(StatusClienteEnum.LEAD.getNome()))
                    .thenReturn(List.of(ativo, inativo));

            List<ClienteListarResponse> resultado = readCliente.listarTodos(true);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals("Cliente Ativo", resultado.get(0).nomeRazaoSocial());
            assertEquals("Cliente Inativo", resultado.get(1).nomeRazaoSocial());
            verify(clienteRepository).findByStatusCliente_NomeNotIgnoreCase("LEAD");
            verify(clienteRepository, never()).findAll();
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Deve buscar cliente por ID com sucesso")
        void deveBuscarClientePorIdComSucesso() {
            Cliente cliente = criarCliente(1L, "Fulano", "ATIVO");
            when(clienteRepository.findByIdComDetalhes(1L)).thenReturn(Optional.of(cliente));

            ClienteDetalhesResponse resultado = readCliente.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals("Fulano", resultado.nomeRazaoSocial());
            verify(authorizationUtil).verificarAcessoCliente(1L);
        }

        @Test
        @DisplayName("Deve lançar ClienteNaoEncontradoException quando não encontrar")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(clienteRepository.findByIdComDetalhes(99L)).thenReturn(Optional.empty());

            assertThrows(ClienteNaoEncontradoException.class, () -> readCliente.buscarPorId(99L));
            verify(authorizationUtil).verificarAcessoCliente(99L);
        }
    }

    @Nested
    @DisplayName("listarPorStatus")
    class ListarPorStatusTest {

        @Test
        @DisplayName("Deve buscar clientes pelo status informado")
        void deveBuscarClientesPorStatus() {
            Cliente cliente = criarCliente(1L, "Fulano", "ATIVO");
            when(clienteRepository.findByStatusCliente_NomeIgnoreCase("ATIVO"))
                    .thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = readCliente.listarPorStatus("ATIVO");

            assertEquals(1, resultado.size());
            assertEquals("Fulano", resultado.getFirst().nomeRazaoSocial());
        }
    }

    @Nested
    @DisplayName("pesquisarPorNomeOuCpfCnpj")
    class PesquisarPorNomeOuCpfCnpjTest {

        @Test
        @DisplayName("Deve delegar busca por nome ou CPF/CNPJ para o repositório")
        void deveBuscarPorNomeOuCpfCnpj() {
            Cliente joao = criarCliente(1L, "João", "ATIVO");

            when(clienteRepository.findByNomeRazaoSocialContainingIgnoreCaseOrCpfCnpjContaining("João", "João"))
                    .thenReturn(List.of(joao));

            List<ClienteListarResponse> resultado = readCliente.pesquisarPorNomeOuCpfCnpj("João");

            assertEquals(1, resultado.size());
            assertEquals("João", resultado.getFirst().nomeRazaoSocial());
        }
    }

    @Nested
    @DisplayName("pesquisarComFiltros")
    class PesquisarComFiltrosTest {

        @Test
        @DisplayName("Deve delegar busca paginada ao repositório com Specification")
        void deveDelegarBuscaComFiltros() {
            Cliente cliente = criarCliente(1L, "Maria", "ATIVO");
            Pageable pageable = PageRequest.of(0, 10);
            Page<Cliente> page = new PageImpl<>(List.of(cliente), pageable, 1);

            when(clienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            Page<ClienteListarResponse> resultado = readCliente.pesquisarComFiltros("Maria", "Todos", "Todos", pageable);

            assertEquals(1, resultado.getTotalElements());
            assertEquals("Maria", resultado.getContent().getFirst().nomeRazaoSocial());
        }
    }
}
