package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService")
class ClienteServiceTest {

    @Mock
    private ClientePersistenciaAdapter clientePersistenciaAdapter;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente criarClienteComStatus(Long id, String nome, String cpfCnpj, String statusNome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNomeRazaoSocial(nome);
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 5, 16, 10, 30));
        cliente.setDataHoraTransformacaoEmCliente(LocalDateTime.of(2026, 5, 16, 14, 0));

        StatusCliente status = new StatusCliente(1L, statusNome);
        cliente.setStatusCliente(status);

        Contato contato = new Contato();
        contato.setId(1L);
        contato.setNome("Contato Principal");
        contato.setEmail("contato@email.com");
        contato.setTelefone("(11) 99999-9999");
        contato.setProfissao("Advogado");
        contato.setPrincipal(true);
        cliente.setContatos(List.of(contato));

        return cliente;
    }

    @Nested
    @DisplayName("listarTodos()")
    class ListarTodos {

        @Test
        @DisplayName("Deve retornar lista vazia quando não existir clientes")
        void deveRetornarListaVaziaSeNaoExistirClientes() {
            when(clientePersistenciaAdapter.listarTodos()).thenReturn(Collections.emptyList());

            List<ClienteListarResponse> resultado = clienteService.listarTodos();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(clientePersistenciaAdapter, times(1)).listarTodos();
        }

        @Test
        @DisplayName("Deve retornar lista com clientes mapeados corretamente")
        void deveRetornarListaComClientesMapeados() {
            Cliente cliente1 = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            Cliente cliente2 = criarClienteComStatus(2L, "Empresa XYZ Ltda", "12345678000199", "Pendente");

            when(clientePersistenciaAdapter.listarTodos()).thenReturn(List.of(cliente1, cliente2));

            List<ClienteListarResponse> resultado = clienteService.listarTodos();

            assertNotNull(resultado);
            assertEquals(2, resultado.size());

            assertEquals(1L, resultado.get(0).id());
            assertEquals("João da Silva", resultado.get(0).nomeRazaoSocial());
            assertEquals("12345678901", resultado.get(0).cpfCnpj());
            assertEquals("contato@email.com", resultado.get(0).email());
            assertEquals("Ativo", resultado.get(0).statusNome());

            assertEquals(2L, resultado.get(1).id());
            assertEquals("Empresa XYZ Ltda", resultado.get(1).nomeRazaoSocial());
            assertEquals("Pendente", resultado.get(1).statusNome());

            verify(clientePersistenciaAdapter, times(1)).listarTodos();
        }

        @Test
        @DisplayName("Deve retornar email nulo quando cliente não possui contato principal")
        void deveRetornarEmailNuloSemContatoPrincipal() {
            Cliente cliente = criarClienteComStatus(1L, "Maria", "98765432100", "Ativo");
            Contato contatoSecundario = new Contato();
            contatoSecundario.setEmail("secundario@email.com");
            contatoSecundario.setPrincipal(false);
            cliente.setContatos(List.of(contatoSecundario));

            when(clientePersistenciaAdapter.listarTodos()).thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = clienteService.listarTodos();

            assertEquals(1, resultado.size());
            assertNull(resultado.get(0).email());
        }

        @Test
        @DisplayName("Deve retornar status nulo quando cliente não possui status")
        void deveRetornarStatusNuloSemStatus() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Sem Status");
            cliente.setStatusCliente(null);
            cliente.setContatos(null);

            when(clientePersistenciaAdapter.listarTodos()).thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = clienteService.listarTodos();

            assertEquals(1, resultado.size());
            assertNull(resultado.get(0).statusNome());
            assertNull(resultado.get(0).email());
        }
    }

    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("Deve retornar detalhes do cliente quando encontrado")
        void deveRetornarDetalhesDoClienteQuandoEncontrado() {
            Cliente cliente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            cliente.setRg("123456789");

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(cliente);

            ClienteDetalhesResponse resultado = clienteService.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.id());
            assertEquals("João da Silva", resultado.nomeRazaoSocial());
            assertEquals("12345678901", resultado.cpfCnpj());
            assertEquals("123456789", resultado.rg());
            assertNotNull(resultado.status());
            assertEquals("Ativo", resultado.status().nome());
            assertNotNull(resultado.contatos());
            assertEquals(1, resultado.contatos().size());
            assertEquals("contato@email.com", resultado.contatos().get(0).email());
            assertTrue(resultado.contatos().get(0).principal());

            verify(clientePersistenciaAdapter, times(1)).buscarPorId(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não for encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clientePersistenciaAdapter.buscarPorId(999L))
                    .thenThrow(new ClienteNaoEncontradoException(999L));

            ClienteNaoEncontradoException exception = assertThrows(
                    ClienteNaoEncontradoException.class,
                    () -> clienteService.buscarPorId(999L)
            );

            assertTrue(exception.getMessage().contains("999"));
            verify(clientePersistenciaAdapter, times(1)).buscarPorId(999L);
        }

        @Test
        @DisplayName("Deve retornar detalhes com endereço e status nulos quando não informados")
        void deveRetornarDetalhesComCamposNulos() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Cliente Simples");
            cliente.setStatusCliente(null);
            cliente.setEndereco(null);
            cliente.setContatos(null);

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(cliente);

            ClienteDetalhesResponse resultado = clienteService.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals("Cliente Simples", resultado.nomeRazaoSocial());
            assertNull(resultado.status());
            assertNull(resultado.endereco());
            assertTrue(resultado.contatos().isEmpty());
        }
    }

    @Nested
    @DisplayName("listarPorStatus()")
    class ListarPorStatus {

        @Test
        @DisplayName("Deve retornar clientes filtrados por status")
        void deveRetornarClientesFiltradosPorStatus() {
            Cliente cliente = criarClienteComStatus(1L, "João", "12345678901", "Ativo");

            when(clientePersistenciaAdapter.listarPorStatus("Ativo")).thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = clienteService.listarPorStatus("Ativo");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("João", resultado.get(0).nomeRazaoSocial());
            assertEquals("Ativo", resultado.get(0).statusNome());

            verify(clientePersistenciaAdapter, times(1)).listarPorStatus("Ativo");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver clientes com o status informado")
        void deveRetornarListaVaziaParaStatusInexistente() {
            when(clientePersistenciaAdapter.listarPorStatus("Inexistente")).thenReturn(Collections.emptyList());

            List<ClienteListarResponse> resultado = clienteService.listarPorStatus("Inexistente");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(clientePersistenciaAdapter, times(1)).listarPorStatus("Inexistente");
        }

        @Test
        @DisplayName("Deve retornar múltiplos clientes com mesmo status")
        void deveRetornarMultiplosClientesComMesmoStatus() {
            Cliente cliente1 = criarClienteComStatus(1L, "Cliente A", "11111111111", "Ativo");
            Cliente cliente2 = criarClienteComStatus(2L, "Cliente B", "22222222222", "Ativo");

            when(clientePersistenciaAdapter.listarPorStatus("Ativo")).thenReturn(List.of(cliente1, cliente2));

            List<ClienteListarResponse> resultado = clienteService.listarPorStatus("Ativo");

            assertEquals(2, resultado.size());
        }
    }

    @Nested
    @DisplayName("pesquisarPorNomeOuCpfCnpj()")
    class PesquisarPorNomeOuCpfCnpj {

        @Test
        @DisplayName("Deve retornar clientes ao pesquisar por nome")
        void deveRetornarClientesAoPesquisarPorNome() {
            Cliente cliente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");

            when(clientePersistenciaAdapter.pesquisarPorNomeOuCpfCnpj("João")).thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = clienteService.pesquisarPorNomeOuCpfCnpj("João");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("João da Silva", resultado.get(0).nomeRazaoSocial());

            verify(clientePersistenciaAdapter, times(1)).pesquisarPorNomeOuCpfCnpj("João");
        }

        @Test
        @DisplayName("Deve retornar clientes ao pesquisar por CPF/CNPJ")
        void deveRetornarClientesAoPesquisarPorCpfCnpj() {
            Cliente cliente = criarClienteComStatus(1L, "Empresa XYZ", "12345678000199", "Ativo");

            when(clientePersistenciaAdapter.pesquisarPorNomeOuCpfCnpj("12345678000199"))
                    .thenReturn(List.of(cliente));

            List<ClienteListarResponse> resultado = clienteService.pesquisarPorNomeOuCpfCnpj("12345678000199");

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals("12345678000199", resultado.get(0).cpfCnpj());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não encontrar resultados")
        void deveRetornarListaVaziaQuandoNaoEncontrar() {
            when(clientePersistenciaAdapter.pesquisarPorNomeOuCpfCnpj("TermoInexistente"))
                    .thenReturn(Collections.emptyList());

            List<ClienteListarResponse> resultado = clienteService.pesquisarPorNomeOuCpfCnpj("TermoInexistente");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(clientePersistenciaAdapter, times(1)).pesquisarPorNomeOuCpfCnpj("TermoInexistente");
        }

        @Test
        @DisplayName("Deve retornar múltiplos clientes quando pesquisa coincide com vários registros")
        void deveRetornarMultiplosResultados() {
            Cliente cliente1 = criarClienteComStatus(1L, "Silva Advogados", "11111111111", "Ativo");
            Cliente cliente2 = criarClienteComStatus(2L, "José Silva", "22222222222", "Pendente");

            when(clientePersistenciaAdapter.pesquisarPorNomeOuCpfCnpj("Silva"))
                    .thenReturn(List.of(cliente1, cliente2));

            List<ClienteListarResponse> resultado = clienteService.pesquisarPorNomeOuCpfCnpj("Silva");

            assertEquals(2, resultado.size());
            assertEquals("Silva Advogados", resultado.get(0).nomeRazaoSocial());
            assertEquals("José Silva", resultado.get(1).nomeRazaoSocial());
        }
    }
}

