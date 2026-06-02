package br.com.sgpj.service;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SolicitacaoDuvidaService")
class SolicitacaoDuvidaServiceTest {

    private static final String OBSERVACAO_SOLICITACAO = "Solicitação enviada pelo formulário público. Estado: SP";

    @Mock private ClienteRepository clienteRepository;
    @Mock private ContatoRepository contatoRepository;
    @Mock private ProcessoAppRepository processoAppRepository;
    @Mock private StatusClienteRepository statusClienteRepository;
    @Mock private ProcessoService processoService;

    private SolicitacaoDuvidaService service;

    @BeforeEach
    void setUp() {
        service = new SolicitacaoDuvidaService(
                clienteRepository,
                contatoRepository,
                processoAppRepository,
                statusClienteRepository,
                processoService
        );

        when(statusClienteRepository.findByNomeIgnoreCase("LEAD")).thenReturn(Optional.of(new StatusCliente(1L, "LEAD")));
        when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(new StatusCliente(2L, "ATIVO")));
        when(statusClienteRepository.findByNomeIgnoreCase("INATIVO")).thenReturn(Optional.of(new StatusCliente(3L, "INATIVO")));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            if (cliente.getId() == null) {
                cliente.setId(10L);
            }
            return cliente;
        });
        when(contatoRepository.save(any(Contato.class))).thenAnswer(invocation -> {
            Contato contato = invocation.getArgument(0);
            contato.setId(20L);
            return contato;
        });
        when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(invocation -> {
            ProcessoApp processo = invocation.getArgument(0);
            if (processo.getId() == null) {
                processo.setId(30L);
            }
            return processo;
        });
    }

    @Nested
    @DisplayName("criar")
    class CriarTest {

        @Test
        @DisplayName("Deve criar solicitação como lead e processo inativo")
        void deveCriarSolicitacaoComoLeadEProcessoInativo() {
            SolicitacaoDuvidaCriacaoRequest request = criarRequest(null);

            SolicitacaoDuvidaDetalhesResponse response = service.criar(request);

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            ArgumentCaptor<Contato> contatoCaptor = ArgumentCaptor.forClass(Contato.class);
            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);

            verify(clienteRepository).save(clienteCaptor.capture());
            verify(contatoRepository).save(contatoCaptor.capture());
            verify(processoAppRepository).save(processoCaptor.capture());
            verify(processoService, never()).buscarDadosOficiais(any(), any());

            Cliente clienteSalvo = clienteCaptor.getValue();
            Contato contatoSalvo = contatoCaptor.getValue();
            ProcessoApp processoSalvo = processoCaptor.getValue();

            assertAll(
                    () -> assertEquals("Maria Silva", clienteSalvo.getNomeRazaoSocial()),
                    () -> assertEquals("LEAD", clienteSalvo.getStatusCliente().getNome()),
                    () -> assertNotNull(clienteSalvo.getDataHoraEnvioFormulario()),
                    () -> assertEquals("Maria Silva", contatoSalvo.getNome()),
                    () -> assertEquals("maria@email.com", contatoSalvo.getEmail()),
                    () -> assertEquals("11999999999", contatoSalvo.getTelefone()),
                    () -> assertTrue(contatoSalvo.getPrincipal()),
                    () -> assertEquals(clienteSalvo, contatoSalvo.getCliente()),
                    () -> assertEquals(clienteSalvo, processoSalvo.getCliente()),
                    () -> assertEquals("Preciso de ajuda com meu processo", processoSalvo.getDescricaoCaso()),
                    () -> assertEquals(OBSERVACAO_SOLICITACAO, processoSalvo.getObservacao()),
                    () -> assertFalse(processoSalvo.getAtivo()),
                    () -> assertEquals(30L, response.id()),
                    () -> assertEquals(10L, response.clienteId()),
                    () -> assertEquals("Maria Silva", response.nomeCompleto()),
                    () -> assertEquals("maria@email.com", response.email()),
                    () -> assertEquals("11999999999", response.telefone()),
                    () -> assertEquals("SP", response.estado()),
                    () -> assertEquals(SolicitacaoStatus.PENDENTE, response.status())
            );
        }

        @Test
        @DisplayName("Deve buscar dados oficiais quando número do processo for informado")
        void deveBuscarDadosOficiaisQuandoNumeroInformado() {
            ProcessoApi processoApi = new ProcessoApi();
            processoApi.setNumeroCnj("10108901220228260292");
            when(processoService.buscarDadosOficiais(any(String.class), any(SiglaTribunal.class))).thenReturn(processoApi);

            SolicitacaoDuvidaDetalhesResponse response = service.criar(criarRequest("10108901220228260292"));

            ArgumentCaptor<SiglaTribunal> tribunalCaptor = ArgumentCaptor.forClass(SiglaTribunal.class);
            verify(processoService).buscarDadosOficiais(org.mockito.ArgumentMatchers.eq("10108901220228260292"), tribunalCaptor.capture());

            assertAll(
                    () -> assertEquals("TJSP", tribunalCaptor.getValue().getNome()),
                    () -> assertEquals("10108901220228260292", response.numeroProcesso()),
                    () -> assertNotNull(response.processo()),
                    () -> assertEquals("10108901220228260292", response.processo().numeroCnj())
            );
        }
    }

    @Nested
    @DisplayName("consultas")
    class ConsultasTest {

        @Test
        @DisplayName("Deve listar solicitações pendentes a partir de clientes lead")
        void deveListarPendentes() {
            ProcessoApp processo = criarProcessoSolicitacao(30L, "LEAD", false);
            when(processoAppRepository.findByCliente_StatusCliente_NomeIgnoreCaseOrderByDataHoraInicioDesc("LEAD"))
                    .thenReturn(List.of(processo));

            List<SolicitacaoDuvidaListarResponse> response = service.listarPendentes();

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(30L, response.getFirst().id()),
                    () -> assertEquals(10L, response.getFirst().clienteId()),
                    () -> assertEquals("Maria Silva", response.getFirst().nomeCompleto()),
                    () -> assertEquals(SolicitacaoStatus.PENDENTE, response.getFirst().status())
            );
        }

        @Test
        @DisplayName("Deve buscar detalhes da solicitação por id")
        void deveBuscarPorId() {
            when(processoAppRepository.findById(30L)).thenReturn(Optional.of(criarProcessoSolicitacao(30L, "LEAD", false)));

            SolicitacaoDuvidaDetalhesResponse response = service.buscarPorId(30L);

            assertAll(
                    () -> assertEquals(30L, response.id()),
                    () -> assertEquals("Maria Silva", response.nomeCompleto()),
                    () -> assertEquals("SP", response.estado()),
                    () -> assertEquals(SolicitacaoStatus.PENDENTE, response.status())
            );
        }
    }

    @Nested
    @DisplayName("decisão")
    class DecisaoTest {

        @Test
        @DisplayName("Deve aceitar solicitação ativando cliente e processo")
        void deveAceitarSolicitacao() {
            ProcessoApp processo = criarProcessoSolicitacao(30L, "LEAD", false);
            when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processo));

            SolicitacaoClientePreenchimentoResponse response = service.aceitar(30L);

            assertAll(
                    () -> assertEquals("ATIVO", processo.getCliente().getStatusCliente().getNome()),
                    () -> assertNotNull(processo.getCliente().getDataHoraTransformacaoEmCliente()),
                    () -> assertTrue(processo.getAtivo()),
                    () -> assertNotNull(processo.getUltimaAtualizacao()),
                    () -> assertEquals(30L, response.solicitacaoId()),
                    () -> assertEquals(10L, response.clienteId()),
                    () -> assertEquals("Maria Silva", response.nomeRazaoSocial())
            );
            verify(clienteRepository).save(processo.getCliente());
            verify(processoAppRepository).save(processo);
        }

        @Test
        @DisplayName("Deve negar solicitação inativando cliente e mantendo processo oculto")
        void deveNegarSolicitacao() {
            ProcessoApp processo = criarProcessoSolicitacao(30L, "LEAD", false);
            when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processo));

            service.negar(30L);

            assertAll(
                    () -> assertEquals("INATIVO", processo.getCliente().getStatusCliente().getNome()),
                    () -> assertNotNull(processo.getCliente().getDataHoraTransformacaoEmCliente()),
                    () -> assertFalse(processo.getAtivo()),
                    () -> assertNotNull(processo.getUltimaAtualizacao())
            );
            verify(clienteRepository).save(processo.getCliente());
            verify(processoAppRepository).save(processo);
        }
    }

    private SolicitacaoDuvidaCriacaoRequest criarRequest(String numeroProcesso) {
        return new SolicitacaoDuvidaCriacaoRequest(
                " Maria Silva ",
                " maria@email.com ",
                " 11999999999 ",
                " sp ",
                numeroProcesso,
                " Preciso de ajuda com meu processo ",
                true
        );
    }

    private ProcessoApp criarProcessoSolicitacao(Long id, String status, boolean ativo) {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Maria Silva");
        cliente.setStatusCliente(new StatusCliente(1L, status));
        cliente.setDataHoraEnvioFormulario(LocalDateTime.of(2026, 5, 30, 10, 0));

        Contato contato = new Contato();
        contato.setId(20L);
        contato.setNome("Maria Silva");
        contato.setEmail("maria@email.com");
        contato.setTelefone("11999999999");
        contato.setPrincipal(true);
        contato.setCliente(cliente);
        cliente.setContatos(List.of(contato));

        ProcessoApp processo = new ProcessoApp();
        processo.setId(id);
        processo.setCliente(cliente);
        processo.setDescricaoCaso("Preciso de ajuda com meu processo");
        processo.setObservacao(OBSERVACAO_SOLICITACAO);
        processo.setDataHoraInicio(cliente.getDataHoraEnvioFormulario());
        processo.setAtivo(ativo);
        return processo;
    }
}
