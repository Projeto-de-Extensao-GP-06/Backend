package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.repository.SiglaTribunalRepository;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import br.com.sgpj.service.useCases.onedrive.GarantirPastasClienteOneDrive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
@DisplayName("AceitarSolicitacao Use Case")
class AceitarSolicitacaoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContatoRepository contatoRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private PrioridadeRepository prioridadeRepository;

    @Mock
    private FaseProcessoRepository faseProcessoRepository;

    @Mock
    private StatusClienteRepository statusClienteRepository;

    @Mock
    private GarantirPastasClienteOneDrive garantirPastasClienteOneDrive;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

    @Mock
    private SiglaTribunalRepository siglaTribunalRepository;

    @Mock
    private SincronizacaoProcessoProducer sincronizacaoProducer;

    private AceitarSolicitacao aceitarSolicitacao;

    private ProcessoApp processoApp;
    private Cliente cliente;
    private StatusCliente statusAtivo;
    private Usuario usuarioResponsavel;

    @BeforeEach
    void setUp() {
        aceitarSolicitacao = new AceitarSolicitacao(processoAppRepository, clienteRepository,
                contatoRepository, enderecoRepository, prioridadeRepository, faseProcessoRepository,
                statusClienteRepository, garantirPastasClienteOneDrive, registroHistorico,
                new PrepararVinculoProcessoOficial(siglaTribunalRepository), sincronizacaoProducer);
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Cliente Teste");

        processoApp = new ProcessoApp();
        processoApp.setId(1L);
        processoApp.setCliente(cliente);
        processoApp.setStatus(StatusProcesso.SOLICITACAO);
        processoApp.setDescricaoCaso("Descrição dúvida");

        statusAtivo = new StatusCliente();
        statusAtivo.setId(1L);
        statusAtivo.setNome("ATIVO");

        usuarioResponsavel = new Usuario();
        usuarioResponsavel.setId(7L);
        usuarioResponsavel.setNome("Usuário responsável");
    }

    private SolicitacaoAceiteRequest requestComCnj(String cnj) {
        SolicitacaoAceiteRequest request = requestPf();
        return new SolicitacaoAceiteRequest(request.client(),
                new SolicitacaoAceiteRequest.Process(cnj, "média", "Inicial", null, null, null));
    }

    private void prepararAceite() {
        prepararReferencias();
        when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
        when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(statusAtivo));
    }

    @Test
    void deveEnfileirarCnjNovoFormatadoComTribunal() {
        prepararAceite();
        SiglaTribunal tribunal = new SiglaTribunal(1L, "TJSP");
        when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.of(tribunal));

        aceitarSolicitacao.aceitar(1L, requestComCnj("0000001-45.2026.8.26.0001"), usuarioResponsavel);

        assertEquals("00000014520268260001", processoApp.getProcessoApi().getNumeroCnj());
        assertSame(tribunal, processoApp.getSiglaTribunal());
        assertEquals("TJSP", processoApp.getProcessoApi().getTribunalTramitacao());
        verify(sincronizacaoProducer).enviarAposCommit(1L);
    }

    @Test
    void deveConsultarCnjSalvoSemDadosMesmoSemCnjNoRequest() {
        prepararAceite();
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000014520268260001");
        processoApi.setTribunalTramitacao("TJSP");
        processoApi.setMovimentacoes(List.of());
        processoApp.setProcessoApi(processoApi);
        processoApp.setSiglaTribunal(new SiglaTribunal(1L, "TJSP"));

        aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);

        assertSame(processoApi, processoApp.getProcessoApi());
        verify(sincronizacaoProducer).enviarAposCommit(1L);
        verifyNoInteractions(siglaTribunalRepository);
    }

    @Test
    void devePreservarCnjSalvoDianteDeOutroCnjNoAceite() {
        prepararAceite();
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000014520268260001");
        processoApp.setProcessoApi(processoApi);
        when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP"))
                .thenReturn(Optional.of(new SiglaTribunal(1L, "TJSP")));

        aceitarSolicitacao.aceitar(1L, requestComCnj("0000001-00.2026.8.19.0001"), usuarioResponsavel);

        assertSame(processoApi, processoApp.getProcessoApi());
        assertEquals("00000014520268260001", processoApi.getNumeroCnj());
        verify(sincronizacaoProducer).enviarAposCommit(1L);
    }

    @Test
    void deveAceitarSemCnjSemEnfileirar() {
        prepararAceite();
        aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);
        verifyNoInteractions(sincronizacaoProducer, siglaTribunalRepository);
    }

    @ParameterizedTest
    @MethodSource("camposOficiais")
    void naoDeveConsultarQuandoPossuiAlgumCampoOficial(Consumer<ProcessoApi> preencherCampo) {
        prepararAceite();
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000014520268260001");
        preencherCampo.accept(processoApi);
        processoApp.setProcessoApi(processoApi);

        aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);

        assertSame(processoApi, processoApp.getProcessoApi());
        verifyNoInteractions(sincronizacaoProducer, siglaTribunalRepository);
    }

    static Stream<Consumer<ProcessoApi>> camposOficiais() {
        return Stream.of(
                processo -> processo.setInstancia("G1"),
                processo -> processo.setDataAjuizamento(LocalDateTime.of(2026, 1, 1, 10, 0)),
                processo -> processo.setNivelSigilo("0"),
                processo -> processo.setClasseProcessual("Classe"),
                processo -> processo.setFormato("Eletrônico"),
                processo -> processo.setSistemaTribunal("Sistema"),
                processo -> processo.setOrgaoJulgador("Vara"),
                processo -> processo.setAssunto("Assunto"),
                processo -> processo.setMovimentacoes(List.of(new MovimentacoesOficiais())));
    }

    @Test
    void deveEnfileirarQuandoCamposOficiaisEstaoEmBranco() {
        prepararAceite();
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000014520268260001");
        processoApi.setInstancia(" ");
        processoApi.setNivelSigilo("");
        processoApi.setMovimentacoes(List.of());
        processoApp.setProcessoApi(processoApi);
        processoApp.setSiglaTribunal(new SiglaTribunal(1L, "TJSP"));
        aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);
        verify(sincronizacaoProducer).enviarAposCommit(1L);
    }

    @Test
    void deveAceitarSemEnfileirarQuandoTribunalNaoEstaCadastrado() {
        prepararAceite();
        when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.empty());
        aceitarSolicitacao.aceitar(1L, requestComCnj("00000014520268260001"), usuarioResponsavel);
        assertEquals(StatusProcesso.ATIVO, processoApp.getStatus());
        assertEquals("00000014520268260001", processoApp.getProcessoApi().getNumeroCnj());
        verifyNoInteractions(sincronizacaoProducer);
    }

    @Test
    void deveAceitarSemEnfileirarQuandoCodigoTribunalNaoEReconhecido() {
        prepararAceite();
        aceitarSolicitacao.aceitar(1L, requestComCnj("00000014520268990001"), usuarioResponsavel);
        assertEquals(StatusProcesso.ATIVO, processoApp.getStatus());
        verifyNoInteractions(sincronizacaoProducer, siglaTribunalRepository);
    }

    @Test
    void naoDeveAgendarSolicitacaoJaProcessada() {
        processoApp.setStatus(StatusProcesso.ATIVO);
        when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
        assertThrows(OperacaoInvalidaException.class,
                () -> aceitarSolicitacao.aceitar(1L, requestComCnj("00000014520268260001"), usuarioResponsavel));
        verifyNoInteractions(sincronizacaoProducer);
    }

    @Test
    void naoDeveAgendarSolicitacaoInexistente() {
        when(processoAppRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ObjetoNaoEncontradoException.class,
                () -> aceitarSolicitacao.aceitar(1L, requestComCnj("00000014520268260001"), usuarioResponsavel));
        verifyNoInteractions(sincronizacaoProducer);
    }

    @Test
    void deveCriarVinculoSeparadoAoPreencherCnjDeVinculoCompartilhadoVazio() {
        prepararAceite();
        ProcessoApi compartilhado = new ProcessoApi();
        compartilhado.setId(42L);
        processoApp.setProcessoApi(compartilhado);
        when(processoAppRepository.existsByProcessoApi_IdAndIdNot(42L, 1L)).thenReturn(true);
        when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP"))
                .thenReturn(Optional.of(new SiglaTribunal(1L, "TJSP")));
        aceitarSolicitacao.aceitar(1L, requestComCnj("00000014520268260001"), usuarioResponsavel);
        assertNull(compartilhado.getNumeroCnj());
        assertEquals("00000014520268260001", processoApp.getProcessoApi().getNumeroCnj());
        verify(sincronizacaoProducer).enviarAposCommit(1L);
    }

        private void prepararReferencias() {
        when(prioridadeRepository.findByNomeIgnoreCase("MEDIA"))
            .thenReturn(Optional.of(new Prioridade(2L, "MEDIA")));
        when(faseProcessoRepository.findByNomeIgnoreCase("INICIAL"))
            .thenReturn(Optional.of(new FaseProcesso(1L, "INICIAL")));
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contatoRepository.save(any(Contato.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        private SolicitacaoAceiteRequest requestPf() {
        return new SolicitacaoAceiteRequest(
            new SolicitacaoAceiteRequest.Client(
                "PF", "Nome informado", null, "novo@email.com", "(11) 99999-9999",
                "52998224725", "123456789", null, "Advogada",
                new SolicitacaoAceiteRequest.Address("01001-000", "Praça da Sé", "100", null,
                    "Sé", "São Paulo", "SP"), List.of()),
            new SolicitacaoAceiteRequest.Process(null, "média", "Inicial", null, null,
                new BigDecimal("250.00")));
        }

        private SolicitacaoAceiteRequest requestPj() {
        return new SolicitacaoAceiteRequest(
            new SolicitacaoAceiteRequest.Client(
                "PJ", "Empresa Alpha LTDA", "Empresa Alpha LTDA", "novo@empresa.com", "11999999999",
                null, null, "11222333000181", "Diretora",
                new SolicitacaoAceiteRequest.Address("01001000", "Nova Rua", "200", null,
                    "Centro", "São Paulo", "SP"),
                List.of(new SolicitacaoAceiteRequest.Contact("Novo Contato", "novo@empresa.com",
                    "11999999999", "Diretora", true))),
            new SolicitacaoAceiteRequest.Process(null, "média", "Inicial", null, null, null));
        }

    @Nested
    @DisplayName("aceitar")
    class AceitarTest {

        @Test
        @DisplayName("Deve aceitar solicitação, mudar status do processo para ATIVO e cliente para ATIVO")
        void deveAceitarSolicitacaoComStatusAtivo() {
            prepararReferencias();
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.of(statusAtivo));

            SolicitacaoClientePreenchimentoResponse response = aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);
            verify(garantirPastasClienteOneDrive).garantir(cliente, 1L);
            verify(registroHistorico).registrar(processoApp, usuarioResponsavel,
                    RegistroHistoricoProcesso.ABERTURA, "Processo criado.");

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());

            assertEquals(StatusProcesso.ATIVO, processoCaptor.getValue().getStatus());
            assertEquals(usuarioResponsavel, processoCaptor.getValue().getUsuarioResponsavel());
            assertNotNull(processoCaptor.getValue().getUltimaAtualizacao());
            assertEquals(statusAtivo, clienteCaptor.getValue().getStatusCliente());
            assertNotNull(clienteCaptor.getValue().getDataHoraTransformacaoEmCliente());
            assertNotNull(response);
            assertEquals(1L, response.processoId());
            assertEquals("ATIVO", response.statusCliente());
            assertEquals("ATIVO", response.statusProcesso());
            assertEquals("11999999999", cliente.getContatos().getFirst().getTelefone());
        }

        @Test
        @DisplayName("Deve preencher corretamente nomeRazaoSocial, nomeContato e cnpj para solicitação de PJ")
        void deveAceitarSolicitacaoPjPreenchendoRazaoSocialNomeContatoECnpj() {
            Cliente clientePj = new Cliente();
            clientePj.setId(20L);
            clientePj.setNomeRazaoSocial("Empresa Alpha LTDA");
            clientePj.setPF(false);

            Contato contato = new Contato();
            contato.setId(100L);
            contato.setNome("Roberto Representante");
            contato.setEmail("roberto@empresaalpha.com");
            contato.setTelefone("11988887777");
            contato.setPrincipal(true);
            clientePj.setContatos(List.of(contato));

            ProcessoApp processoPj = new ProcessoApp();
            processoPj.setId(2L);
            processoPj.setCliente(clientePj);
            processoPj.setStatus(StatusProcesso.SOLICITACAO);
            processoPj.setCnpjInformadoFormulario("12345678000195");
            processoPj.setObservacao("Solicitação enviada pelo formulário público. Estado: SP");

            prepararReferencias();
            when(processoAppRepository.findById(2L)).thenReturn(Optional.of(processoPj));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.of(statusAtivo));

            SolicitacaoClientePreenchimentoResponse response = aceitarSolicitacao.aceitar(2L, requestPj(), usuarioResponsavel);

            assertNotNull(response);
            assertEquals("Empresa Alpha LTDA", response.nomeRazaoSocial());
            assertEquals("Roberto Representante", response.nomeContato());
            assertEquals("12345678000195", response.cnpj());
            assertEquals("roberto@empresaalpha.com", response.email());
            assertEquals("11988887777", response.telefone());
            assertEquals("SP", response.estado());
            assertEquals("Roberto Representante", contato.getNome());
            assertEquals("roberto@empresaalpha.com", contato.getEmail());
            assertEquals("11988887777", contato.getTelefone());
        }

        @Test
        @DisplayName("Deve retornar cnpj nulo e nome de contato correto para solicitação de PF")
        void deveAceitarSolicitacaoPfComCnpjNuloENomeContato() {
            Cliente clientePf = new Cliente();
            clientePf.setId(30L);
            clientePf.setNomeRazaoSocial("Maria Fernandes");
            clientePf.setPF(true);

            Contato contato = new Contato();
            contato.setId(200L);
            contato.setNome("Maria Fernandes");
            contato.setEmail("maria@gmail.com");
            contato.setTelefone("21977776666");
            contato.setPrincipal(true);
            clientePf.setContatos(List.of(contato));

            ProcessoApp processoPf = new ProcessoApp();
            processoPf.setId(3L);
            processoPf.setCliente(clientePf);
            processoPf.setStatus(StatusProcesso.SOLICITACAO);
            processoPf.setCnpjInformadoFormulario(null);
            processoPf.setObservacao("Solicitação enviada pelo formulário público. Estado: RJ");

            prepararReferencias();
            when(processoAppRepository.findById(3L)).thenReturn(Optional.of(processoPf));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.of(statusAtivo));

            SolicitacaoClientePreenchimentoResponse response = aceitarSolicitacao.aceitar(3L, requestPf(), usuarioResponsavel);

            assertNotNull(response);
            assertEquals("Maria Fernandes", response.nomeRazaoSocial());
            assertEquals("Maria Fernandes", response.nomeContato());
            assertNull(response.cnpj());
            assertEquals("maria@gmail.com", response.email());
            assertEquals("21977776666", response.telefone());
            assertEquals("RJ", response.estado());
        }

        @Test
        @DisplayName("Deve lançar exceção quando solicitação não for encontrada")
        void deveLancarExcecaoQuandoSolicitacaoNaoEncontrada() {
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> aceitarSolicitacao.aceitar(99L, requestPf(), usuarioResponsavel));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
            org.mockito.Mockito.verifyNoInteractions(garantirPastasClienteOneDrive);
        }

        @Test
        @DisplayName("Deve lançar exceção quando status cliente ATIVO não existir no banco")
        void deveLancarExcecaoQuandoStatusClienteNaoEncontrado() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve preservar endereço, contato e data de cliente ativo com outro processo")
        void devePreservarDadosDeClienteAtivoComOutroProcesso() {
            prepararReferencias();
            LocalDateTime dataTransformacao = LocalDateTime.of(2024, 1, 15, 10, 0);
            cliente.setPF(true);
            cliente.setStatusCliente(statusAtivo);
            cliente.setDataHoraTransformacaoEmCliente(dataTransformacao);
            Endereco endereco = new Endereco();
            endereco.setCep("22041001");
            endereco.setLogradouro("Rua existente");
            endereco.setNumero("10");
            endereco.setBairro("Copacabana");
            endereco.setCidade("Rio de Janeiro");
            endereco.setEstado("RJ");
            cliente.setEndereco(endereco);
            Contato contato = new Contato();
            contato.setNome("Contato existente");
            contato.setEmail("existente@email.com");
            contato.setTelefone("21988887777");
            contato.setPrincipal(true);
            cliente.setContatos(List.of(contato));
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(true);

            aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel);

            assertEquals(dataTransformacao, cliente.getDataHoraTransformacaoEmCliente());
            assertEquals("22041001", cliente.getEndereco().getCep());
            assertEquals("Rua existente", cliente.getEndereco().getLogradouro());
            assertEquals("Contato existente", cliente.getContatos().getFirst().getNome());
            assertEquals("existente@email.com", cliente.getContatos().getFirst().getEmail());
            verify(statusClienteRepository, never()).findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome());
            verify(processoAppRepository).existsByCliente_IdAndIdNot(10L, 1L);
        }

        @Test
        @DisplayName("Deve rejeitar aceite repetido sem persistir alterações")
        void deveRejeitarAceiteRepetido() {
            processoApp.setStatus(StatusProcesso.ATIVO);
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));

            assertThrows(OperacaoInvalidaException.class, () -> aceitarSolicitacao.aceitar(1L, requestPf(), usuarioResponsavel));
            org.mockito.Mockito.verifyNoInteractions(garantirPastasClienteOneDrive);

            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }
    }
}
