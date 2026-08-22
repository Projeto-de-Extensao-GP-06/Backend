package br.com.sgpj.service;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.SiglaTribunalRepository;
import br.com.sgpj.security.AuthorizationUtil;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProcessoService")
class ProcessoServiceTest {

    @Mock private UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    @Mock private DataJudAdapter dataJudAdapter;
    @Mock private ProcessoAppRepository processoAppRepository;
    @Mock private PrioridadeRepository prioridadeRepository;
    @Mock private FaseProcessoRepository faseProcessoRepository;
    @Mock private SiglaTribunalRepository siglaTribunalRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private HistoricoProcessoRepository historicoProcessoRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private ProcessoDocumentoRepository processoDocumentoRepository;
    @Mock private AuthorizationUtil authorizationUtil;

    private ProcessoService service;

    @BeforeEach
    void setUp() {
        service = new ProcessoService(
                usuarioPersistenciaAdapter,
                dataJudAdapter,
                processoAppRepository,
                prioridadeRepository,
                faseProcessoRepository,
                siglaTribunalRepository,
                clienteRepository,
                historicoProcessoRepository,
                documentoRepository,
                processoDocumentoRepository,
                authorizationUtil
        );
    }

    @Nested
    @DisplayName("buscarDadosOficiais")
    class BuscarDadosOficiaisTest {

        @Test
        @DisplayName("Deve retornar nulo quando o número do processo estiver em branco")
        void deveRetornarNuloQuandoNumeroEmBranco() {
            SiglaTribunal siglaTribunal = new SiglaTribunal(1L, "TJSP");

            ProcessoApi resultado = service.buscarDadosOficiais("   ", siglaTribunal);

            assertNull(resultado);
            verifyNoInteractions(dataJudAdapter);
        }

        @Test
        @DisplayName("Deve converter o JSON da API em ProcessoApi")
        void deveConverterJsonEmProcessoApi() {
            SiglaTribunal siglaTribunal = new SiglaTribunal(1L, "TJSP");
            when(dataJudAdapter.buscarPorNumeroDeProcesso("10108901220228260292", "TJSP"))
                    .thenReturn(jsonDataJud());

            ProcessoApi resultado = service.buscarDadosOficiais("10108901220228260292", siglaTribunal);

            assertAll(
                    () -> assertEquals("10108901220228260292", resultado.getNumeroCnj()),
                    () -> assertEquals("Classe Principal", resultado.getClasseProcessual()),
                    () -> assertEquals("Sistema Eletronico", resultado.getSistemaTribunal()),
                    () -> assertEquals("PJe", resultado.getFormato()),
                    () -> assertEquals("TJSP", resultado.getTribunalTramitacao()),
                    () -> assertEquals("1G", resultado.getInstancia()),
                    () -> assertEquals("Familia", resultado.getAssunto()),
                    () -> assertEquals(2, resultado.getMovimentacoes().size()),
                    () -> assertEquals("Concluso para decisao", resultado.getMovimentacoes().get(1).getDescricao())
            );

            verify(dataJudAdapter).buscarPorNumeroDeProcesso("10108901220228260292", "TJSP");
        }
    }

    @Nested
    @DisplayName("criarProcesso")
    class CriarProcessoTest {

        @Test
        @DisplayName("Deve salvar o processo com os dados oficiais quando o número do processo for informado")
        void deveSalvarProcessoComDadosOficiais() {
            Usuario usuario = criarUsuario(1L, "Usuario Responsavel");
            Cliente cliente = criarCliente(2L, "Cliente Teste");
            Prioridade prioridade = new Prioridade(3L, "Alta");
            FaseProcesso faseProcesso = new FaseProcesso(4L, "Inicial");
            SiglaTribunal siglaTribunal = new SiglaTribunal(5L, "TJSP");
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    usuario.getId(),
                    cliente.getId(),
                    "Descricao do caso com tamanho suficiente",
                    "Observacao do processo com tamanho suficiente",
                    prioridade.getId(),
                    faseProcesso.getId(),
                    LocalDateTime.of(2026, 5, 28, 10, 0),
                    siglaTribunal.getId(),
                    "10108901220228260292",
                    2500.0
            );

            when(usuarioPersistenciaAdapter.buscarUsuarioPorId(1L)).thenReturn(usuario);
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
            when(prioridadeRepository.findById(3L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(4L)).thenReturn(Optional.of(faseProcesso));
            when(siglaTribunalRepository.findById(5L)).thenReturn(Optional.of(siglaTribunal));
            when(dataJudAdapter.buscarPorNumeroDeProcesso("10108901220228260292", "TJSP"))
                    .thenReturn(jsonDataJud());
            when(processoAppRepository.save(any(ProcessoApp.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProcessoAppCreateResponse response = service.criarProcesso(request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertAll(
                    () -> assertEquals(usuario, salvo.getUsuarioResponsavel()),
                    () -> assertEquals(cliente, salvo.getCliente()),
                    () -> assertEquals(prioridade, salvo.getPrioridade()),
                    () -> assertEquals(faseProcesso, salvo.getFaseProcesso()),
                    () -> assertEquals(siglaTribunal, salvo.getSiglaTribunal()),
                    () -> assertNotNull(salvo.getProcessoApi()),
                    () -> assertEquals("10108901220228260292", salvo.getProcessoApi().getNumeroCnj()),
                    () -> assertEquals(2, salvo.getProcessoApi().getMovimentacoes().size())
            );

            assertAll(
                    () -> assertEquals(cliente.getId(), response.clienteId()),
                    () -> assertEquals(usuario.getId(), response.usuarioResponsavelId()),
                    () -> assertEquals(prioridade.getId(), response.prioridade().getId()),
                    () -> assertEquals(faseProcesso.getId(), response.faseProcesso().getId()),
                    () -> assertEquals(siglaTribunal.getId(), response.siglaTribunal().getId()),
                    () -> assertNotNull(response.processoApi()),
                    () -> assertEquals("10108901220228260292", response.processoApi().numeroCnj()),
                    () -> assertEquals(2, response.processoApi().movimentacoes().size())
            );
        }
    }

    @Nested
    @DisplayName("atualizarProcesso")
    class AtualizarProcessoTest {

        @Test
        @DisplayName("Deve preservar o ProcessoApi existente e encerrar o processo quando ficar inativo")
        void devePreservarProcessoApiExistenteEEncerrarQuandoInativo() {
            ProcessoApp processoEncontrado = criarProcessoBase(10L, true);
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "10108901220228260292",
                    "Descricao atualizada do caso com tamanho suficiente",
                    "Observacao atualizada do processo com tamanho suficiente",
                    3L,
                    false,
                    4L,
                    LocalDateTime.of(2026, 5, 29, 11, 0),
                    4000.0
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processoEncontrado));
            when(prioridadeRepository.findById(3L)).thenReturn(Optional.of(new Prioridade(3L, "Alta")));
            when(faseProcessoRepository.findById(4L)).thenReturn(Optional.of(new FaseProcesso(4L, "Inicial")));
            when(processoAppRepository.save(any(ProcessoApp.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProcessoAppUpdateResponse response = service.atualizarProcesso(10L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertAll(
                    () -> assertSame(processoEncontrado.getProcessoApi(), salvo.getProcessoApi()),
                    () -> assertNotNull(salvo.getEncerradoEm()),
                    () -> assertEquals(processoEncontrado.getUsuarioResponsavel(), salvo.getEncerradoPorUsuario()),
                    () -> assertEquals(false, salvo.getAtivo())
            );

            assertAll(
                    () -> assertEquals(processoEncontrado.getProcessoApi(), response.processoApi()),
                    () -> assertNotNull(response.dataHoraEncerramento()),
                    () -> assertEquals(processoEncontrado.getUsuarioResponsavel().getId(), response.encerradoPorUsuarioId())
            );

            verifyNoInteractions(dataJudAdapter);
        }

        @Test
        @DisplayName("Deve buscar o ProcessoApi quando o processo atual não tiver dados oficiais")
        void deveBuscarProcessoApiQuandoNaoExistir() {
            ProcessoApp processoEncontrado = criarProcessoBase(11L, false);
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "10108901220228260292",
                    "Descricao atualizada do caso com tamanho suficiente",
                    "Observacao atualizada do processo com tamanho suficiente",
                    3L,
                    true,
                    4L,
                    LocalDateTime.of(2026, 5, 29, 11, 0),
                    4000.0
            );

            when(processoAppRepository.findById(11L)).thenReturn(Optional.of(processoEncontrado));
            when(prioridadeRepository.findById(3L)).thenReturn(Optional.of(new Prioridade(3L, "Alta")));
            when(faseProcessoRepository.findById(4L)).thenReturn(Optional.of(new FaseProcesso(4L, "Inicial")));
            when(dataJudAdapter.buscarPorNumeroDeProcesso("10108901220228260292", "TJSP"))
                    .thenReturn(jsonDataJud());
            when(processoAppRepository.save(any(ProcessoApp.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ProcessoAppUpdateResponse response = service.atualizarProcesso(11L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertAll(
                    () -> assertNotNull(salvo.getProcessoApi()),
                    () -> assertEquals("10108901220228260292", salvo.getProcessoApi().getNumeroCnj()),
                    () -> assertNull(salvo.getEncerradoEm()),
                    () -> assertEquals(true, salvo.getAtivo())
            );

            assertAll(
                    () -> assertNotNull(response.processoApi()),
                    () -> assertEquals("10108901220228260292", response.processoApi().getNumeroCnj()),
                    () -> assertNull(response.dataHoraEncerramento())
            );

            verify(dataJudAdapter).buscarPorNumeroDeProcesso("10108901220228260292", "TJSP");
        }
    }

    @Nested
    @DisplayName("consultas")
    class ConsultasTest {

        @Test
        @DisplayName("Deve ocultar solicitações ainda não aceitas da lista de processos")
        void deveOcultarSolicitacoesAindaNaoAceitasDaLista() {
            ProcessoApp processoNormal = criarProcessoBase(20L, true);
            ProcessoApp solicitacaoPendente = criarProcessoSolicitacao(21L, false);
            ProcessoApp solicitacaoAceita = criarProcessoSolicitacao(22L, true);
            when(processoAppRepository.findAll()).thenReturn(List.of(processoNormal, solicitacaoPendente, solicitacaoAceita));

            List<ProcessoAppMiniResponse> response = service.listarProcessos();

            assertAll(
                    () -> assertEquals(2, response.size()),
                    () -> assertEquals(20L, response.get(0).id()),
                    () -> assertEquals(22L, response.get(1).id())
            );
        }

        @Test
        @DisplayName("Deve listar processos mini response")
        void deveListarProcessos() {
            ProcessoApp processo = criarProcessoBase(20L, true);
            when(processoAppRepository.findAll()).thenReturn(List.of(processo));

            List<ProcessoAppMiniResponse> response = service.listarProcessos();

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(processo.getId(), response.get(0).id()),
                    () -> assertEquals(processo.getCliente().getId(), response.get(0).clienteId()),
                    () -> assertEquals(processo.getProcessoApi().getNumeroCnj(), response.get(0).numeroProcesso())
            );
        }

        @Test
        @DisplayName("Deve buscar processo por id com histórico e dados da API")
        void deveBuscarPorIdComHistorico() {
            ProcessoApp processo = criarProcessoBase(30L, true);
            HistoricoProcesso historico = criarHistoricoProcesso(70L, processo);
            doNothing().when(authorizationUtil).verificarAcessoProcesso(30L);
            when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processo));
            when(historicoProcessoRepository.findByProcessoApp(processo)).thenReturn(List.of(historico));

            ProcessoAppResponse response = service.buscarPorId(30L);

            assertAll(
                    () -> assertEquals(processo.getId(), response.id()),
                    () -> assertEquals(processo.getCliente().getId(), response.clienteId()),
                    () -> assertNotNull(response.processoApi()),
                    () -> assertEquals("10108901220228260292", response.processoApi().numeroCnj()),
                    () -> assertEquals("Concluso para decisao", response.processoApi().ultimoMovimento().descricao()),
                    () -> assertEquals(1, response.historicoProcessoResponses().size()),
                    () -> assertEquals("Mudanca de fase", response.historicoProcessoResponses().get(0).descricaoAlteracao())
            );
            
            verify(authorizationUtil, times(1)).verificarAcessoProcesso(30L);
        }

        @Test
        @DisplayName("Deve tratar solicitação pendente como processo não encontrado")
        void deveTratarSolicitacaoPendenteComoProcessoNaoEncontrado() {
            ProcessoApp solicitacaoPendente = criarProcessoSolicitacao(31L, false);
            doNothing().when(authorizationUtil).verificarAcessoProcesso(31L);
            when(processoAppRepository.findById(31L)).thenReturn(Optional.of(solicitacaoPendente));

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.buscarPorId(31L));
            verify(authorizationUtil, times(1)).verificarAcessoProcesso(31L);
            verifyNoInteractions(historicoProcessoRepository);
        }

        @Test
        @DisplayName("Deve permitir buscar solicitação aceita como processo")
        void devePermitirBuscarSolicitacaoAceitaComoProcesso() {
            ProcessoApp solicitacaoAceita = criarProcessoSolicitacao(32L, true);
            doNothing().when(authorizationUtil).verificarAcessoProcesso(32L);
            when(processoAppRepository.findById(32L)).thenReturn(Optional.of(solicitacaoAceita));
            when(historicoProcessoRepository.findByProcessoApp(solicitacaoAceita)).thenReturn(List.of());

            ProcessoAppResponse response = service.buscarPorId(32L);

            assertAll(
                    () -> assertEquals(32L, response.id()),
                    () -> assertEquals(solicitacaoAceita.getCliente().getId(), response.clienteId()),
                    () -> assertEquals("10108901220228260292", response.processoApi().numeroCnj())
            );
            
            verify(authorizationUtil, times(1)).verificarAcessoProcesso(32L);
        }

        @Test
        @DisplayName("Deve buscar processos por cliente")
        void deveBuscarPorClienteId() {
            Cliente cliente = criarCliente(2L, "Cliente Teste");
            ProcessoApp processo = criarProcessoBase(40L, true);
            doNothing().when(authorizationUtil).verificarAcessoCliente(2L);
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
            when(processoAppRepository.findByCliente(cliente)).thenReturn(List.of(processo));

            List<ProcessoAppMiniResponse> response = service.buscarPorClienteId(2L);

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(cliente.getId(), response.get(0).clienteId()),
                    () -> assertEquals("10108901220228260292", response.get(0).numeroProcesso())
            );
            
            verify(authorizationUtil, times(1)).verificarAcessoCliente(2L);
        }

        @Test
        @DisplayName("Deve ocultar solicitações pendentes na busca por cliente")
        void deveOcultarSolicitacoesPendentesNaBuscaPorCliente() {
            Cliente cliente = criarCliente(2L, "Cliente Teste");
            ProcessoApp solicitacaoPendente = criarProcessoSolicitacao(41L, false);
            ProcessoApp solicitacaoAceita = criarProcessoSolicitacao(42L, true);
            doNothing().when(authorizationUtil).verificarAcessoCliente(2L);
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
            when(processoAppRepository.findByCliente(cliente)).thenReturn(List.of(solicitacaoPendente, solicitacaoAceita));

            List<ProcessoAppMiniResponse> response = service.buscarPorClienteId(2L);

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(42L, response.get(0).id())
            );
            
            verify(authorizationUtil, times(1)).verificarAcessoCliente(2L);
        }

        @Test
        @DisplayName("Deve normalizar a query e repassar os filtros")
        void deveNormalizarQueryERepassarFiltros() {
            ProcessoApp processo = criarProcessoBase(50L, true);
            when(processoAppRepository.buscaFiltro(null, 4L, 5L)).thenReturn(List.of(processo));

            List<ProcessoAppMiniResponse> response = service.buscaFiltro("   ", 4L, 5L);

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(processo.getId(), response.get(0).id()),
                    () -> assertEquals("10108901220228260292", response.get(0).numeroProcesso())
            );

            verify(processoAppRepository).buscaFiltro(null, 4L, 5L);
        }

        @Test
        @DisplayName("Deve ocultar solicitações pendentes na busca por filtro")
        void deveOcultarSolicitacoesPendentesNaBuscaPorFiltro() {
            ProcessoApp solicitacaoPendente = criarProcessoSolicitacao(51L, false);
            ProcessoApp solicitacaoAceita = criarProcessoSolicitacao(52L, true);
            when(processoAppRepository.buscaFiltro("Maria", null, null)).thenReturn(List.of(solicitacaoPendente, solicitacaoAceita));

            List<ProcessoAppMiniResponse> response = service.buscaFiltro(" Maria ", null, null);

            assertAll(
                    () -> assertEquals(1, response.size()),
                    () -> assertEquals(52L, response.get(0).id())
            );
            verify(processoAppRepository).buscaFiltro("Maria", null, null);
        }
    }

    @Nested
    @DisplayName("anexarDocumento")
    class AnexarDocumentoTest {

        @Test
        @DisplayName("Deve vincular documento ao processo e retornar response")
        void deveVincularDocumentoAoProcesso() {
            ProcessoApp processo = criarProcessoBase(10L, true);
            Documento documento = criarDocumentoMock(20L, "contrato.pdf");
            Usuario usuario = criarUsuario(1L, "Usuario Responsavel");

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(documentoRepository.findById(20L)).thenReturn(Optional.of(documento));
            when(usuarioPersistenciaAdapter.buscarUsuarioPorId(1L)).thenReturn(usuario);
            when(processoDocumentoRepository.existsById(any())).thenReturn(false);
            when(processoDocumentoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProcessoDocumentoResponse response = service.anexarDocumento(10L, 20L, 1L);

            assertAll(
                    () -> assertEquals(20L, response.documentoId()),
                    () -> assertEquals("contrato.pdf", response.nomeOriginal()),
                    () -> assertEquals(1L, response.anexadoPorUsuarioId()),
                    () -> assertEquals("Usuario Responsavel", response.anexadoPorUsuarioNome()),
                    () -> assertNotNull(response.anexadoEm())
            );
            verify(processoDocumentoRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando processo não encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.anexarDocumento(99L, 1L, 1L));
            verifyNoInteractions(documentoRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção quando documento não encontrado")
        void deveLancarExcecaoQuandoDocumentoNaoEncontrado() {
            ProcessoApp processo = criarProcessoBase(10L, true);
            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(documentoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.anexarDocumento(10L, 99L, 1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando documento já está vinculado ao processo")
        void deveLancarExcecaoQuandoJaVinculado() {
            ProcessoApp processo = criarProcessoBase(10L, true);
            Documento documento = criarDocumentoMock(20L, "contrato.pdf");
            Usuario usuario = criarUsuario(1L, "Usuario");

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(documentoRepository.findById(20L)).thenReturn(Optional.of(documento));
            when(usuarioPersistenciaAdapter.buscarUsuarioPorId(1L)).thenReturn(usuario);
            when(processoDocumentoRepository.existsById(any())).thenReturn(true);

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.anexarDocumento(10L, 20L, 1L));
            verify(processoDocumentoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("listarDocumentosDoProcesso")
    class ListarDocumentosDoProcessoTest {

        @Test
        @DisplayName("Deve retornar lista de documentos vinculados ao processo")
        void deveRetornarListaDeDocumentos() {
            ProcessoApp processo = criarProcessoBase(10L, true);
            Documento documento = criarDocumentoMock(20L, "contrato.pdf");
            Usuario usuario = criarUsuario(1L, "Usuario Responsavel");

            ProcessoDocumentoId pdId = new ProcessoDocumentoId(20L, 10L);
            ProcessoDocumento pd = new ProcessoDocumento();
            pd.setId(pdId);
            pd.setDocumento(documento);
            pd.setProcessoApp(processo);
            pd.setAnexadoPorUsuario(usuario);
            pd.setAnexadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));

            doNothing().when(authorizationUtil).verificarAcessoProcesso(10L);
            when(processoDocumentoRepository.findByProcessoAppId(10L)).thenReturn(List.of(pd));

            List<ProcessoDocumentoResponse> result = service.listarDocumentosDoProcesso(10L);

            assertAll(
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals(20L, result.get(0).documentoId()),
                    () -> assertEquals("contrato.pdf", result.get(0).nomeOriginal()),
                    () -> assertEquals(1L, result.get(0).anexadoPorUsuarioId()),
                    () -> assertEquals("Usuario Responsavel", result.get(0).anexadoPorUsuarioNome())
            );
            
            verify(authorizationUtil, times(1)).verificarAcessoProcesso(10L);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando processo sem documentos")
        void deveRetornarListaVaziaQuandoSemDocumentos() {
            doNothing().when(authorizationUtil).verificarAcessoProcesso(10L);
            when(processoDocumentoRepository.findByProcessoAppId(10L)).thenReturn(List.of());

            List<ProcessoDocumentoResponse> result = service.listarDocumentosDoProcesso(10L);

            assertTrue(result.isEmpty());
            
            verify(authorizationUtil, times(1)).verificarAcessoProcesso(10L);
        }
    }

    @Nested
    @DisplayName("deletarProcesso")
    class DelecaoTest {

        @Test
        @DisplayName("Deve deletar quando o processo existir")
        void deveDeletarQuandoExistir() {
            when(processoAppRepository.existsById(10L)).thenReturn(true);

            service.deletarProcesso(10L);

            verify(processoAppRepository).deleteById(10L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando o processo não existir")
        void deveLancarExcecaoQuandoNaoExistir() {
            when(processoAppRepository.existsById(99L)).thenReturn(false);

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.deletarProcesso(99L));
            verify(processoAppRepository, never()).deleteById(99L);
        }
    }

    private ProcessoApp criarProcessoBase(Long id, boolean comProcessoApi) {
        ProcessoApp processo = new ProcessoApp();
        processo.setId(id);
        processo.setUsuarioResponsavel(criarUsuario(1L, "Usuario Responsavel"));
        processo.setCliente(criarCliente(2L, "Cliente Teste"));
        processo.setDescricaoCaso("Descricao do caso com tamanho suficiente");
        processo.setObservacao("Observacao do processo com tamanho suficiente");
        processo.setPrioridade(new Prioridade(3L, "Alta"));
        processo.setFaseProcesso(new FaseProcesso(4L, "Inicial"));
        processo.setUltimaAtualizacao(LocalDateTime.of(2026, 5, 28, 10, 0));
        processo.setDataHoraInicio(LocalDateTime.of(2026, 5, 28, 9, 0));
        processo.setAtivo(true);
        processo.setDataAudiencia(LocalDateTime.of(2026, 5, 29, 11, 0));
        processo.setSiglaTribunal(new SiglaTribunal(5L, "TJSP"));
        processo.setValorCausa(2500.0);

        if (comProcessoApi) {
            processo.setProcessoApi(criarProcessoApi(90L, "10108901220228260292"));
        }

        return processo;
    }

    private ProcessoApp criarProcessoSolicitacao(Long id, boolean aceita) {
        ProcessoApp processo = criarProcessoBase(id, true);
        processo.setObservacao("Solicitação enviada pelo formulário público. Estado: SP");
        processo.setAtivo(aceita);
        return processo;
    }

    private ProcessoApi criarProcessoApi(Long id, String numeroCnj) {
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setId(id);
        processoApi.setNumeroCnj(numeroCnj);
        processoApi.setTribunalTramitacao("TJSP");
        processoApi.setInstancia("1G");
        processoApi.setDataAjuizamento(LocalDateTime.of(2024, 1, 2, 11, 22, 33));
        processoApi.setNivelSigilo(0);
        processoApi.setClasseProcessual("Classe Principal");
        processoApi.setFormato("PJe");
        processoApi.setSistemaTribunal("Sistema Eletronico");
        processoApi.setOrgaoJulgador("10a Vara");
        processoApi.setAssunto("Familia");

        MovimentacoesOficiais movimento1 = new MovimentacoesOficiais();
        movimento1.setId(1L);
        movimento1.setCodigo(10);
        movimento1.setDescricao("Distribuido");
        movimento1.setDataHoraMovimentacao(LocalDateTime.of(2024, 1, 2, 10, 15));
        movimento1.setProcessoApi(processoApi);

        MovimentacoesOficiais movimento2 = new MovimentacoesOficiais();
        movimento2.setId(2L);
        movimento2.setCodigo(20);
        movimento2.setDescricao("Concluso para decisao");
        movimento2.setDataHoraMovimentacao(LocalDateTime.of(2024, 1, 3, 11, 0));
        movimento2.setProcessoApi(processoApi);

        processoApi.setMovimentacoes(List.of(movimento1, movimento2));
        return processoApi;
    }

    private HistoricoProcesso criarHistoricoProcesso(Long id, ProcessoApp processoApp) {
        HistoricoProcesso historico = new HistoricoProcesso();
        historico.setId(id);
        historico.setProcessoApp(processoApp);
        historico.setUsuarioUtilizado(criarUsuario(9L, "Usuario Historico"));
        historico.setDescricaoAlteracao("Mudanca de fase");
        historico.setDataHoraAlteracao(LocalDateTime.of(2026, 5, 28, 12, 0));
        historico.setTipoAlteracao(new TipoAlteracao(11L, "ALTERACAO"));
        return historico;
    }

    private Cliente criarCliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNomeRazaoSocial(nome);
        cliente.setCpfCnpj("12345678901");
        return cliente;
    }

    private Usuario criarUsuario(Long id, String nome) {
        return new Usuario(id, nome, nome.toLowerCase().replace(" ", ".") + "@email.com", null, null, LocalDateTime.of(2024, 1, 1, 0, 0), true);
    }

    private Documento criarDocumentoMock(Long id, String nome) {
        Documento doc = new Documento();
        doc.setId(id);
        doc.setNomeOriginal(nome);
        doc.setMimeType("application/pdf");
        doc.setTamanhoBytes(1024L);
        doc.setAtivo(true);
        return doc;
    }

    private static String jsonDataJud() {
        return """
                {
                  "hits": {
                    "hits": [
                      {
                        "_source": {
                          "numeroProcesso": "10108901220228260292",
                          "classe": { "nome": "Classe Principal" },
                          "sistema": { "nome": "Sistema Eletronico" },
                          "formato": { "nome": "PJe" },
                          "tribunal": "TJSP",
                          "grau": "1G",
                          "assuntos": [
                            { "nome": "Familia" }
                          ],
                          "dataAjuizamento": "20240102112233",
                          "nivelSigilo": 0,
                          "orgaoJulgador": { "nome": "10a Vara" },
                          "movimentos": [
                            {
                              "codigo": 10,
                              "nome": "Distribuido",
                              "dataHora": "2024-01-02T10:15:00Z"
                            },
                            {
                              "codigo": 20,
                              "nome": "Concluso para decisao",
                              "dataHora": "2024-01-03T11:00:00Z"
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
                """;
    }
}
