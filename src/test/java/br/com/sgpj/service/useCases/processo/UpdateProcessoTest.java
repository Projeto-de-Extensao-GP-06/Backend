package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.SiglaTribunalRepository;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProcesso Use Case")
class UpdateProcessoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private PrioridadeRepository prioridadeRepository;

    @Mock
    private FaseProcessoRepository faseProcessoRepository;

    @Mock
    private SiglaTribunalRepository siglaTribunalRepository;

    @Mock
    private ProcessoApiGateway processoApiGateway;

    @Mock
    private SincronizacaoProcessoProducer sincronizacaoProducer;

    @Mock
    private MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @InjectMocks
    private UpdateProcesso updateProcesso;

    private ProcessoApp processoExistente;
    private Prioridade prioridade;
    private FaseProcesso faseProcesso;
    private Usuario usuarioResponsavel;
    private Cliente cliente;
    private SiglaTribunal tribunal;

    @BeforeEach
    void setUp() {
        usuarioResponsavel = new Usuario();
        usuarioResponsavel.setId(100L);
        usuarioResponsavel.setNome("Advogado");

        cliente = new Cliente();
        cliente.setId(200L);
        cliente.setNomeRazaoSocial("Cliente");

        tribunal = new SiglaTribunal(1L, "TJSP");
        prioridade = new Prioridade(1L, "NORMAL");
        faseProcesso = new FaseProcesso(1L, "INICIAL");

        processoExistente = new ProcessoApp();
        processoExistente.setId(1L);
        processoExistente.setUsuarioResponsavel(usuarioResponsavel);
        processoExistente.setCliente(cliente);
        processoExistente.setPrioridade(prioridade);
        processoExistente.setFaseProcesso(faseProcesso);
        processoExistente.setSiglaTribunal(tribunal);
        processoExistente.setStatus(StatusProcesso.ATIVO);
        processoExistente.setDataHoraInicio(LocalDateTime.now().minusDays(30));
        lenient().when(authorizationUtil.getUsuarioLogado()).thenReturn(usuarioResponsavel);
    }

    @Nested
    @DisplayName("Validação de Entidades Não Encontradas")
    class ValidacoesEntidadesNaoEncontradas {

        @Test
        @DisplayName("Deve lançar exceção quando processo não for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0, null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prioridade não for encontrada")
        void deveLancarExcecaoQuandoPrioridadeNaoEncontrada() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 99L, StatusProcesso.ATIVO, 1L, null, 1000.0, null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fase do processo não for encontrada")
        void deveLancarExcecaoQuandoFaseNaoEncontrada() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ATIVO, 99L, null, 1000.0, null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Atualização para SUSPENSO")
    class AtualizacaoParaSuspenso {

        @Test
        @DisplayName("Deve lançar exceção quando motivoStatusProcessoId for nulo ao suspender")
        void deveLancarExcecaoQuandoMotivoNuloAoSuspender() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, null, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));

            OperacaoInvalidaException exception = assertThrows(OperacaoInvalidaException.class,
                    () -> updateProcesso.atualizarProcesso(1L, request));

            assertEquals("O motivo é obrigatório ao suspender ou encerrar um processo.", exception.getMessage());
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo de status não for encontrado no banco")
        void deveLancarExcecaoQuandoMotivoNaoEncontrado() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 999, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(999)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo for aplicável apenas a ENCERRADO")
        void deveLancarExcecaoQuandoMotivoIncompativelComSuspensao() {
            MotivoStatusProcesso motivoEncerrado = new MotivoStatusProcesso();
            motivoEncerrado.setId(1);
            motivoEncerrado.setNome("TRANSITO_EM_JULGADO");
            motivoEncerrado.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 1, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(1)).thenReturn(Optional.of(motivoEncerrado));

            OperacaoInvalidaException exception = assertThrows(OperacaoInvalidaException.class,
                    () -> updateProcesso.atualizarProcesso(1L, request));

            assertEquals("O motivo selecionado não é aplicável ao status do processo.", exception.getMessage());
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve suspender processo com sucesso quando motivo for aplicável a SUSPENSO")
        void deveSuspenderComSucessoMotivoSuspenso() {
            MotivoStatusProcesso motivoSuspenso = new MotivoStatusProcesso();
            motivoSuspenso.setId(2);
            motivoSuspenso.setNome("AGUARDANDO_DECISAO");
            motivoSuspenso.setAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 2, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(2)).thenReturn(Optional.of(motivoSuspenso));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            ProcessoAppUpdateResponse response = updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertEquals(StatusProcesso.SUSPENSO, salvo.getStatus());
            assertEquals(motivoSuspenso, salvo.getMotivoStatus());
            assertNotNull(salvo.getEncerradoSuspensoEm());
            assertEquals(usuarioResponsavel, salvo.getEncerradoSuspensoPorUsuario());
            assertEquals(StatusProcesso.SUSPENSO, response.status());
            assertEquals(2, response.motivoStatusId());
        }

        @Test
        @DisplayName("Deve suspender processo com sucesso quando motivo for aplicável a AMBOS")
        void deveSuspenderComSucessoMotivoAmbos() {
            MotivoStatusProcesso motivoAmbos = new MotivoStatusProcesso();
            motivoAmbos.setId(3);
            motivoAmbos.setNome("OUTROS");
            motivoAmbos.setAplicavelA(MotivoStatusProcesso.AplicavelA.AMBOS);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 3, "Outros motivos", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(3)).thenReturn(Optional.of(motivoAmbos));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            ProcessoAppUpdateResponse response = updateProcesso.atualizarProcesso(1L, request);

            assertEquals(StatusProcesso.SUSPENSO, response.status());
            assertEquals(3, response.motivoStatusId());
        }
    }

    @Nested
    @DisplayName("Atualização para ENCERRADO")
    class AtualizacaoParaEncerrado {

        @Test
        @DisplayName("Deve lançar exceção quando motivoStatusProcessoId for nulo ao encerrar")
        void deveLancarExcecaoQuandoMotivoNuloAoEncerrar() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, null, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));

            OperacaoInvalidaException exception = assertThrows(OperacaoInvalidaException.class,
                    () -> updateProcesso.atualizarProcesso(1L, request));

            assertEquals("O motivo é obrigatório ao suspender ou encerrar um processo.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando motivo for aplicável apenas a SUSPENSO")
        void deveLancarExcecaoQuandoMotivoIncompativelComEncerramento() {
            MotivoStatusProcesso motivoSuspenso = new MotivoStatusProcesso();
            motivoSuspenso.setId(2);
            motivoSuspenso.setNome("AGUARDANDO_DECISAO");
            motivoSuspenso.setAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, 2, "Justificativa", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(2)).thenReturn(Optional.of(motivoSuspenso));

            OperacaoInvalidaException exception = assertThrows(OperacaoInvalidaException.class,
                    () -> updateProcesso.atualizarProcesso(1L, request));

            assertEquals("O motivo selecionado não é aplicável ao status do processo.", exception.getMessage());
        }

        @Test
        @DisplayName("Deve encerrar processo com sucesso quando motivo for aplicável a ENCERRADO")
        void deveEncerrarComSucessoMotivoEncerrado() {
            MotivoStatusProcesso motivoEncerrado = new MotivoStatusProcesso();
            motivoEncerrado.setId(1);
            motivoEncerrado.setNome("TRANSITO_EM_JULGADO");
            motivoEncerrado.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, 1, "Justificativa encerramento", null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(motivoStatusProcessoRepository.findById(1)).thenReturn(Optional.of(motivoEncerrado));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            ProcessoAppUpdateResponse response = updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertEquals(StatusProcesso.ENCERRADO, salvo.getStatus());
            assertEquals(motivoEncerrado, salvo.getMotivoStatus());
            assertNotNull(salvo.getEncerradoSuspensoEm());
            assertEquals(usuarioResponsavel, salvo.getEncerradoSuspensoPorUsuario());
            assertEquals(StatusProcesso.ENCERRADO, response.status());
            assertEquals(1, response.motivoStatusId());
        }
    }

    @Nested
    @DisplayName("Reativação para ATIVO")
    class ReativacaoParaAtivo {

        @Test
        @DisplayName("Deve limpar campos de encerramento/suspensão ao reativar processo para ATIVO")
        void deveLimparCamposDeSuspensaoAoReativar() {
            processoExistente.setStatus(StatusProcesso.SUSPENSO);
            processoExistente.setEncerradoSuspensoEm(LocalDateTime.now().minusDays(5));
            processoExistente.setEncerradoSuspensoPorUsuario(usuarioResponsavel);
            processoExistente.setJustificativa("Motivo anterior");

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc atualizada", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 2000.0, null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            ProcessoAppUpdateResponse response = updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertEquals(StatusProcesso.ATIVO, salvo.getStatus());
            assertNull(salvo.getEncerradoSuspensoEm());
            assertNull(salvo.getEncerradoSuspensoPorUsuario());
            assertNull(salvo.getMotivoStatus());
            assertNull(salvo.getJustificativa());

            assertEquals(StatusProcesso.ATIVO, response.status());
            assertNull(response.encerradoSuspensoEm());
            assertNull(response.encerradoSuspensoPorUsuarioId());
            assertNull(response.motivoStatusId());
            assertNull(response.justificativa());
        }
    }

    @Nested
    @DisplayName("Integração com ProcessoApi e SiglaTribunal")
    class IntegracaoProcessoApi {

        @Test
        @DisplayName("Deve atribuir sigla do tribunal pelo CNJ, criar ProcessoApi mínimo e enfileirar sincronização quando não existir processoApi anterior")
        void deveBuscarProcessoOficialQuandoNaoExisteENumeroCnjInformado() {
            processoExistente.setProcessoApi(null);
            processoExistente.setSiglaTribunal(null);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "00000010020268260001", "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0,
                    null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.of(tribunal));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());
            assertEquals("00000010020268260001", captor.getValue().getProcessoApi().getNumeroCnj());
            assertEquals("TJSP", captor.getValue().getProcessoApi().getTribunalTramitacao());
            assertEquals(tribunal, captor.getValue().getSiglaTribunal());
            verify(processoApiGateway, never()).buscarDadosOficiaisOuFalhar(any(), any());
            verify(sincronizacaoProducer).enviarAposCommit(1L);
        }

        @Test
        @DisplayName("Deve manter processoApi e siglaTribunal existentes sem permitir alteração de CNJ quando já estiver associado")
        void deveManterProcessoApiExistente() {
            ProcessoApi apiExistente = new ProcessoApi();
            apiExistente.setNumeroCnj("99999999920268260001");
            processoExistente.setProcessoApi(apiExistente);
            processoExistente.setSiglaTribunal(tribunal);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "00000010020268190001", "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0,
                    null, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());
            assertEquals(apiExistente, captor.getValue().getProcessoApi());
            assertEquals("99999999920268260001", captor.getValue().getProcessoApi().getNumeroCnj());
            assertEquals(tribunal, captor.getValue().getSiglaTribunal());
            verify(processoApiGateway, never()).buscarDadosOficiaisOuFalhar(any(), any());
            verify(sincronizacaoProducer, never()).enviarAposCommit(any());
        }
    }

    @Nested
    @DisplayName("Descrição personalizada da alteração")
    class DescricaoAlteracao {

        private void prepararMocks() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));
        }

        private ProcessoAppUpdateRequest requestComDescricao(String descricaoAlteracao) {
            return new ProcessoAppUpdateRequest(
                    null, "Desc", null, 1L, StatusProcesso.ATIVO, 1L, null, 1000.0, null, null, descricaoAlteracao);
        }

        private void assertHistoricoAtualizacao(String descricaoEsperada) {
            verify(registroHistorico).registrar(any(ProcessoApp.class), eq(usuarioResponsavel),
                    eq(RegistroHistoricoProcesso.ATUALIZACAO), eq(descricaoEsperada));
        }

        @Test
        @DisplayName("Deve registrar a descrição personalizada quando informada")
        void deveRegistrarDescricaoPersonalizada() {
            prepararMocks();

            updateProcesso.atualizarProcesso(1L, requestComDescricao("Altera fase do processo."));

            assertHistoricoAtualizacao("Altera fase do processo.");
        }

        @Test
        @DisplayName("Deve remover espaços nas extremidades da descrição")
        void deveAplicarTrimNaDescricao() {
            prepararMocks();

            updateProcesso.atualizarProcesso(1L, requestComDescricao("  Ajuste de prioridade  "));

            assertHistoricoAtualizacao("Ajuste de prioridade");
        }

        @Test
        @DisplayName("Deve registrar mensagem padrão quando descrição for nula")
        void deveRegistrarMensagemPadraoQuandoNula() {
            prepararMocks();

            updateProcesso.atualizarProcesso(1L, requestComDescricao(null));

            assertHistoricoAtualizacao("Dados do processo atualizados.");
        }

        @Test
        @DisplayName("Deve registrar mensagem padrão quando descrição for vazia")
        void deveRegistrarMensagemPadraoQuandoVazia() {
            prepararMocks();

            updateProcesso.atualizarProcesso(1L, requestComDescricao(""));

            assertHistoricoAtualizacao("Dados do processo atualizados.");
        }

        @Test
        @DisplayName("Deve registrar mensagem padrão quando descrição contiver apenas espaços")
        void deveRegistrarMensagemPadraoQuandoEmBranco() {
            prepararMocks();

            updateProcesso.atualizarProcesso(1L, requestComDescricao("   "));

            assertHistoricoAtualizacao("Dados do processo atualizados.");
        }

        @Test
        @DisplayName("Deve ignorar a descrição personalizada quando o status for alterado")
        void deveIgnorarDescricaoQuandoStatusAlterado() {
            MotivoStatusProcesso motivo = new MotivoStatusProcesso();
            motivo.setId(3);
            motivo.setNome("OUTROS");
            motivo.setAplicavelA(MotivoStatusProcesso.AplicavelA.AMBOS);
            prepararMocks();
            when(motivoStatusProcessoRepository.findById(3)).thenReturn(Optional.of(motivo));

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", null, 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 3, "Just", "Minha descrição");

            updateProcesso.atualizarProcesso(1L, request);

            verify(registroHistorico).registrar(any(ProcessoApp.class), eq(usuarioResponsavel),
                    eq(RegistroHistoricoProcesso.STATUS), eq("Status alterado de ATIVO para SUSPENSO."));
            verify(registroHistorico, never()).registrar(any(), any(), eq(RegistroHistoricoProcesso.ATUALIZACAO), any());
        }
    }
}
