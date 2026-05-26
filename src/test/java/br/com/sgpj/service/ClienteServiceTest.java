package br.com.sgpj.service;

import br.com.sgpj.adapter.database.ClientePersistenciaAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.ClienteJaExistenteException;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.repository.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService")
class ClienteServiceTest {

    @Mock
    private ClientePersistenciaAdapter clientePersistenciaAdapter;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private StatusClienteRepository statusClienteRepository;
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private PadraoCaminhoArmazenamentoRepository padraoCaminhoArmazenamentoRepository;
    @Mock
    private DocumentoRepository documentoRepository;
    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock
    private OneDriveService oneDriveService;

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

    private OneDriveItemResponse criarOneDriveItemResponse(String nomePasta) {
        return new OneDriveItemResponse(
                "drive-item-id-123",
                nomePasta,
                "pasta",
                null,
                null,
                null,
                null,
                null,
                null,
                "sgpj/usuarios/" + nomePasta
        );
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

    @Nested
    @DisplayName("criar()")
    class Criar {

        @Test
        @DisplayName("Deve criar cliente com sucesso e criar pasta no OneDrive")
        void deveCriarClienteComSucesso() {
            ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                    "João da Silva", "12345678901", "123456789", 1L, null, null
            );

            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");
            Cliente clienteSalvo = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail("admin@email.com");
            TipoDocumento tipoDocumento = new TipoDocumento(1L, "OUTROS");
            when(clienteRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteSalvo);
            when(oneDriveService.criarPasta(anyString())).thenReturn(criarOneDriveItemResponse("joao-da-silva-12345678901"));
            when(padraoCaminhoArmazenamentoRepository.save(any(PadraoCaminhoArmazenamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@email.com");
            when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuario));
            when(tipoDocumentoRepository.findByNomeIgnoreCase("OUTROS")).thenReturn(Optional.of(tipoDocumento));
            when(documentoRepository.save(any(Documento.class))).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteSalvo);

            ClienteDetalhesResponse resultado = clienteService.criar(request);

            assertNotNull(resultado);
            assertEquals("João da Silva", resultado.nomeRazaoSocial());
            verify(oneDriveService).criarPasta(anyString());
            verify(padraoCaminhoArmazenamentoRepository).save(any(PadraoCaminhoArmazenamento.class));
            verify(documentoRepository).save(any(Documento.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF/CNPJ já existir")
        void deveLancarExcecaoQuandoCpfCnpjJaExistir() {
            ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                    "João da Silva", "12345678901", "123456789", 1L, null, null
            );

            when(clienteRepository.existsByCpfCnpj("12345678901")).thenReturn(true);

            assertThrows(ClienteJaExistenteException.class, () -> clienteService.criar(request));

            verify(oneDriveService, never()).criarPasta(anyString());
            verify(clientePersistenciaAdapter, never()).salvar(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve criar cliente com endereço")
        void deveCriarClienteComEndereco() {
            ClienteCriacaoRequest.EnderecoRequest enderecoReq = new ClienteCriacaoRequest.EnderecoRequest(
                    "01001000", "Praça da Sé", "100", "Sé", "São Paulo", "SP", "Sala 101"
            );
            ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                    "Maria Santos", "98765432100", null, 1L, enderecoReq, null
            );

            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");
            Cliente clienteSalvo = criarClienteComStatus(1L, "Maria Santos", "98765432100", "Ativo");
            Usuario usuario = new Usuario();
            usuario.setEmail("admin@email.com");
            TipoDocumento tipoDocumento = new TipoDocumento(1L, "OUTROS");

            when(clienteRepository.existsByCpfCnpj("98765432100")).thenReturn(false);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(enderecoRepository.save(any(Endereco.class))).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteSalvo);
            when(oneDriveService.criarPasta(anyString())).thenReturn(criarOneDriveItemResponse("maria-santos-98765432100"));
            when(padraoCaminhoArmazenamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@email.com");
            when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuario));
            when(tipoDocumentoRepository.findByNomeIgnoreCase("OUTROS")).thenReturn(Optional.of(tipoDocumento));
            when(documentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteSalvo);

            ClienteDetalhesResponse resultado = clienteService.criar(request);

            assertNotNull(resultado);
            verify(enderecoRepository).save(any(Endereco.class));
        }

        @Test
        @DisplayName("Deve criar cliente com contatos")
        void deveCriarClienteComContatos() {
            ClienteCriacaoRequest.ContatoRequest contatoReq = new ClienteCriacaoRequest.ContatoRequest(
                    "Maria", "maria@email.com", "(11) 99999-9999", "Advogada", true
            );
            ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                    "Empresa XYZ", "12345678000199", null, 1L, null, List.of(contatoReq)
            );

            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");
            Cliente clienteSalvo = criarClienteComStatus(1L, "Empresa XYZ", "12345678000199", "Ativo");
            Usuario usuario = new Usuario();
            usuario.setEmail("admin@email.com");
            TipoDocumento tipoDocumento = new TipoDocumento(1L, "OUTROS");

            when(clienteRepository.existsByCpfCnpj("12345678000199")).thenReturn(false);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteSalvo);
            when(contatoRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(oneDriveService.criarPasta(anyString())).thenReturn(criarOneDriveItemResponse("empresa-xyz-12345678000199"));
            when(padraoCaminhoArmazenamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@email.com");
            when(usuarioRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(usuario));
            when(tipoDocumentoRepository.findByNomeIgnoreCase("OUTROS")).thenReturn(Optional.of(tipoDocumento));
            when(documentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteSalvo);

            ClienteDetalhesResponse resultado = clienteService.criar(request);

            assertNotNull(resultado);
            verify(contatoRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status não for encontrado")
        void deveLancarExcecaoQuandoStatusNaoEncontrado() {
            ClienteCriacaoRequest request = new ClienteCriacaoRequest(
                    "João", "12345678901", null, 999L, null, null
            );

            when(clienteRepository.existsByCpfCnpj("12345678901")).thenReturn(false);
            when(statusClienteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> clienteService.criar(request));

            verify(oneDriveService, never()).criarPasta(anyString());
        }
    }

    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar cliente sem renomear pasta quando nome e CPF não mudaram")
        void deveAtualizarSemRenomearPasta() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");

            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João da Silva", "12345678901", "999999999", 1L, null, null
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteExistente);

            ClienteDetalhesResponse resultado = clienteService.atualizar(1L, request);

            assertNotNull(resultado);
            verify(oneDriveService, never()).renomearPasta(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve renomear pasta no OneDrive quando nome mudar")
        void deveRenomearPastaQuandoNomeMudar() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");
            PadraoCaminhoArmazenamento caminho = new PadraoCaminhoArmazenamento("joao-da-silva-12345678901", "sgpj/usuarios/joao-da-silva-12345678901");
            Documento documento = new Documento();
            documento.setNomeOriginal("joao-da-silva-12345678901");

            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João Silva Santos", "12345678901", "123456789", 1L, null, null
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteExistente);
            when(oneDriveService.renomearPasta(anyString(), anyString()))
                    .thenReturn(criarOneDriveItemResponse("joao-silva-santos-12345678901"));
            when(padraoCaminhoArmazenamentoRepository.findByNome(anyString()))
                    .thenReturn(Optional.of(caminho));
            when(padraoCaminhoArmazenamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(documentoRepository.findByPadraoCaminhoArmazenamento(caminho))
                    .thenReturn(Optional.of(documento));
            when(documentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ClienteDetalhesResponse resultado = clienteService.atualizar(1L, request);

            assertNotNull(resultado);
            verify(oneDriveService).renomearPasta(anyString(), anyString());
            verify(padraoCaminhoArmazenamentoRepository).save(any());
            verify(documentoRepository).save(any());
        }

        @Test
        @DisplayName("Deve renomear pasta no OneDrive quando CPF/CNPJ mudar")
        void deveRenomearPastaQuandoCpfMudar() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");
            PadraoCaminhoArmazenamento caminho = new PadraoCaminhoArmazenamento("joao-da-silva-12345678901", "sgpj/usuarios/joao-da-silva-12345678901");

            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João da Silva", "99988877766", "123456789", 1L, null, null
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(clienteRepository.existsByCpfCnpj("99988877766")).thenReturn(false);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(clientePersistenciaAdapter.salvar(any(Cliente.class))).thenReturn(clienteExistente);
            when(oneDriveService.renomearPasta(anyString(), anyString()))
                    .thenReturn(criarOneDriveItemResponse("joao-da-silva-99988877766"));
            when(padraoCaminhoArmazenamentoRepository.findByNome(anyString()))
                    .thenReturn(Optional.of(caminho));
            when(padraoCaminhoArmazenamentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(documentoRepository.findByPadraoCaminhoArmazenamento(caminho))
                    .thenReturn(Optional.empty());

            clienteService.atualizar(1L, request);

            verify(oneDriveService).renomearPasta(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF/CNPJ já pertence a outro cliente")
        void deveLancarExcecaoQuandoCpfJaPertenceAOutro() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");

            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João da Silva", "99999999999", null, 1L, null, null
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(clienteRepository.existsByCpfCnpj("99999999999")).thenReturn(true);

            assertThrows(ClienteJaExistenteException.class, () -> clienteService.atualizar(1L, request));

            verify(oneDriveService, never()).renomearPasta(anyString(), anyString());
            verify(clientePersistenciaAdapter, never()).salvar(any());
        }

        @Test
        @DisplayName("Deve atualizar endereço do cliente")
        void deveAtualizarEndereco() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João", "12345678901", "Ativo");
            clienteExistente.setEndereco(new Endereco());
            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");

            ClienteAtualizacaoRequest.EnderecoRequest enderecoReq = new ClienteAtualizacaoRequest.EnderecoRequest(
                    "02002000", "Rua Nova", "200", "Centro", "Rio de Janeiro", "RJ", null
            );
            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João", "12345678901", null, 1L, enderecoReq, null
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(enderecoRepository.save(any(Endereco.class))).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.salvar(any())).thenReturn(clienteExistente);

            clienteService.atualizar(1L, request);

            verify(enderecoRepository).save(any(Endereco.class));
        }

        @Test
        @DisplayName("Deve atualizar contatos - remover antigos e salvar novos")
        void deveAtualizarContatos() {
            Cliente clienteExistente = criarClienteComStatus(1L, "João", "12345678901", "Ativo");
            StatusCliente statusCliente = new StatusCliente(1L, "Ativo");

            ClienteAtualizacaoRequest.ContatoRequest contatoReq = new ClienteAtualizacaoRequest.ContatoRequest(
                    "Novo Contato", "novo@email.com", "(11) 88888-8888", "Engenheiro", true
            );
            ClienteAtualizacaoRequest request = new ClienteAtualizacaoRequest(
                    "João", "12345678901", null, 1L, null, List.of(contatoReq)
            );

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(clienteExistente);
            when(statusClienteRepository.findById(1L)).thenReturn(Optional.of(statusCliente));
            when(contatoRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
            when(clientePersistenciaAdapter.salvar(any())).thenReturn(clienteExistente);

            clienteService.atualizar(1L, request);

            verify(contatoRepository).deleteAll(anyList());
            verify(contatoRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("Deve deletar cliente, documentos e pasta no OneDrive")
        void deveDeletarClienteComSucesso() {
            Cliente cliente = criarClienteComStatus(1L, "João da Silva", "12345678901", "Ativo");
            PadraoCaminhoArmazenamento caminho = new PadraoCaminhoArmazenamento("pasta", "sgpj/usuarios/pasta");
            Documento documento = new Documento();

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(cliente);
            when(padraoCaminhoArmazenamentoRepository.findByNome(anyString())).thenReturn(Optional.of(caminho));
            when(documentoRepository.findByPadraoCaminhoArmazenamento(caminho)).thenReturn(Optional.of(documento));

            clienteService.deletar(1L);

            verify(contatoRepository).deleteAll(anyList());
            verify(documentoRepository).delete(documento);
            verify(padraoCaminhoArmazenamentoRepository).delete(caminho);
            verify(clientePersistenciaAdapter).deletar(1L);
            verify(oneDriveService).excluirPasta(anyString());
        }

        @Test
        @DisplayName("Deve deletar cliente sem contatos")
        void deveDeletarClienteSemContatos() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Maria");
            cliente.setCpfCnpj("98765432100");
            cliente.setContatos(null);
            String nomePasta = "maria-98765432100";

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(cliente);
            when(padraoCaminhoArmazenamentoRepository.findByNome(nomePasta)).thenReturn(Optional.empty());

            clienteService.deletar(1L);

            verify(contatoRepository, never()).deleteAll(anyList());
            verify(clientePersistenciaAdapter).deletar(1L);
            verify(oneDriveService).excluirPasta(nomePasta);
        }

        @Test
        @DisplayName("Deve deletar cliente quando não existir documento associado")
        void deveDeletarSemDocumentoAssociado() {
            Cliente cliente = criarClienteComStatus(1L, "Pedro", "11122233344", "Ativo");
            String nomePasta = "pedro-11122233344";
            PadraoCaminhoArmazenamento caminho = new PadraoCaminhoArmazenamento(nomePasta, "sgpj/usuarios/" + nomePasta);

            when(clientePersistenciaAdapter.buscarPorId(1L)).thenReturn(cliente);
            when(padraoCaminhoArmazenamentoRepository.findByNome(nomePasta)).thenReturn(Optional.of(caminho));
            when(documentoRepository.findByPadraoCaminhoArmazenamento(caminho)).thenReturn(Optional.empty());

            clienteService.deletar(1L);

            verify(documentoRepository, never()).delete(any(Documento.class));
            verify(padraoCaminhoArmazenamentoRepository).delete(caminho);
            verify(clientePersistenciaAdapter).deletar(1L);
            verify(oneDriveService).excluirPasta(nomePasta);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não for encontrado para deletar")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clientePersistenciaAdapter.buscarPorId(999L))
                    .thenThrow(new ClienteNaoEncontradoException(999L));

            assertThrows(ClienteNaoEncontradoException.class, () -> clienteService.deletar(999L));

            verify(oneDriveService, never()).excluirPasta(anyString());
        }
    }
}
