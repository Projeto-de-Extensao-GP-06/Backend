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
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.SiglaTribunalRepository;
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
import static org.mockito.ArgumentMatchers.nullable;
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
    private MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

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
    }

    @Nested
    @DisplayName("Validação de Entidades Não Encontradas")
    class ValidacoesEntidadesNaoEncontradas {

        @Test
        @DisplayName("Deve lançar exceção quando processo não for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prioridade não for encontrada")
        void deveLancarExcecaoQuandoPrioridadeNaoEncontrada() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 99L, StatusProcesso.ATIVO, 1L, null, 1000.0, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> updateProcesso.atualizarProcesso(1L, request));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando fase do processo não for encontrada")
        void deveLancarExcecaoQuandoFaseNaoEncontrada() {
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Desc", "Obs", 1L, StatusProcesso.ATIVO, 99L, null, 1000.0, null, null);

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
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, null, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 999, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 1, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 2, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.SUSPENSO, 1L, null, 1000.0, 3, "Outros motivos");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, null, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, 2, "Justificativa");

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
                    null, "Desc", "Obs", 1L, StatusProcesso.ENCERRADO, 1L, null, 1000.0, 1, "Justificativa encerramento");

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
    @DisplayName("Atualização de tribunal")
    class AtualizacaoTribunal {

        @Test
        @DisplayName("Deve persistir a sigla de tribunal selecionada")
        void deveAtualizarSiglaTribunal() {
            SiglaTribunal tribunalSelecionado = new SiglaTribunal(2L, "TJRJ");
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    null, "Descrição atualizada", "Observação atualizada", 1L,
                    StatusProcesso.ATIVO, 1L, 2L, null, 1000.0, null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(siglaTribunalRepository.findById(2L)).thenReturn(Optional.of(tribunalSelecionado));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(invocation -> invocation.getArgument(0));

            updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());
            assertEquals(tribunalSelecionado, captor.getValue().getSiglaTribunal());
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
                    null, "Desc atualizada", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 2000.0, null, null);

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
    @DisplayName("Integração com ProcessoApi")
    class IntegracaoProcessoApi {

        @Test
        @DisplayName("Deve buscar processo oficial quando não existir processoApi anterior e CNJ for informado")
        void deveBuscarProcessoOficialQuandoNaoExisteENumeroCnjInformado() {
            processoExistente.setProcessoApi(null);
            ProcessoApi apiMock = new ProcessoApi();
            apiMock.setNumeroCnj("0000001-00.2026.8.26.0001");

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "0000001-00.2026.8.26.0001", "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0,
                    null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(processoApiGateway.buscarDadosOficiais(eq("0000001-00.2026.8.26.0001"), nullable(SiglaTribunal.class))).thenReturn(apiMock);
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());
            assertEquals(apiMock, captor.getValue().getProcessoApi());
            verify(processoApiGateway).buscarDadosOficiais(eq("0000001-00.2026.8.26.0001"), nullable(SiglaTribunal.class));
        }

        @Test
        @DisplayName("Deve manter processoApi existente quando já estiver associado")
        void deveManterProcessoApiExistente() {
            ProcessoApi apiExistente = new ProcessoApi();
            apiExistente.setNumeroCnj("9999999-99.2026.8.26.0001");
            processoExistente.setProcessoApi(apiExistente);

            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "0000001-00.2026.8.26.0001", "Desc", "Obs", 1L, StatusProcesso.ATIVO, 1L, null, 1000.0,
                    null, null);

            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoExistente));
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(1L)).thenReturn(Optional.of(faseProcesso));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> inv.getArgument(0));

            updateProcesso.atualizarProcesso(1L, request);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());
            assertEquals(apiExistente, captor.getValue().getProcessoApi());
            verify(processoApiGateway, never()).buscarDadosOficiais(any());
        }
    }
}
