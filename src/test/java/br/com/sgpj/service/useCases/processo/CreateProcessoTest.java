package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.TribunalInvalidoException;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateProcesso Use Case")
class CreateProcessoTest {

    @Mock
    private PrioridadeRepository prioridadeRepository;

    @Mock
    private FaseProcessoRepository faseProcessoRepository;

    @Mock
    private SiglaTribunalRepository siglaTribunalRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SincronizacaoProcessoProducer sincronizacaoProducer;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

    @InjectMocks
    private CreateProcesso createProcesso;

    private Usuario usuarioAutenticado;
    private Prioridade prioridade;
    private FaseProcesso faseProcesso;
    private SiglaTribunal siglaTribunal;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(10L);
        usuarioAutenticado.setNome("Advogado");

        prioridade = new Prioridade(1L, "ALTA");
        faseProcesso = new FaseProcesso(2L, "INICIAL");
        siglaTribunal = new SiglaTribunal(3L, "TJSP");

        cliente = new Cliente();
        cliente.setId(4L);
        cliente.setNomeRazaoSocial("Cliente Teste");
    }

    @Nested
    @DisplayName("criarProcesso")
    class CriarProcessoTest {

        @Test
        @DisplayName("Deve criar processo com status ATIVO com sucesso sem processo oficial")
        void deveCriarProcessoComStatusAtivoSemProcessoOficial() {
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    4L, "Descrição", "Obs", 1L, 2L,
                    LocalDateTime.of(2026, 12, 1, 14, 0),
                    null, 10000.0
            );

            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(2L)).thenReturn(Optional.of(faseProcesso));
            when(clienteRepository.findById(4L)).thenReturn(Optional.of(cliente));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(100L);
                return p;
            });

            ProcessoAppCreateResponse response = createProcesso.criarProcesso(request, usuarioAutenticado);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals(StatusProcesso.ATIVO, salvo.getStatus());
            assertEquals(StatusProcesso.ATIVO, response.status());
            assertEquals(usuarioAutenticado, salvo.getUsuarioResponsavel());
            assertEquals(cliente, salvo.getCliente());
            assertEquals(prioridade, salvo.getPrioridade());
            assertEquals(faseProcesso, salvo.getFaseProcesso());
            assertNull(salvo.getSiglaTribunal());
            verify(sincronizacaoProducer, never()).enviarAposCommit(any());
        }

        @Test
        @DisplayName("Deve criar processo com ProcessoApi mínimo e enfileirar sincronização quando número do processo for fornecido")
        void deveCriarProcessoEEnfileirarSincronizacaoQuandoNumeroProcessoInformado() {
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    4L, "Descrição", "Obs", 1L, 2L,
                    LocalDateTime.of(2026, 12, 1, 14, 0),
                    "00000010020268260001", 10000.0
            );

            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(2L)).thenReturn(Optional.of(faseProcesso));
            when(siglaTribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.of(siglaTribunal));
            when(clienteRepository.findById(4L)).thenReturn(Optional.of(cliente));
            when(processoAppRepository.save(any(ProcessoApp.class))).thenAnswer(inv -> {
                ProcessoApp p = inv.getArgument(0);
                p.setId(100L);
                return p;
            });

            ProcessoAppCreateResponse response = createProcesso.criarProcesso(request, usuarioAutenticado);

            ArgumentCaptor<ProcessoApp> captor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(captor.capture());

            ProcessoApp salvo = captor.getValue();
            assertNotNull(salvo.getProcessoApi());
            assertEquals("00000010020268260001", salvo.getProcessoApi().getNumeroCnj());
            assertEquals("TJSP", salvo.getProcessoApi().getTribunalTramitacao());
            assertEquals(siglaTribunal, salvo.getSiglaTribunal());
            assertEquals(StatusProcesso.ATIVO, salvo.getStatus());
            assertEquals(Boolean.FALSE, response.sincronizado());
            verify(sincronizacaoProducer).enviarAposCommit(100L);
        }

        @Test
        @DisplayName("Deve lançar TribunalInvalidoException quando código do tribunal no CNJ for inválido")
        void deveLancarExcecaoQuandoCodigoTribunalInvalido() {
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    4L, "Descrição", "Obs", 1L, 2L, null, "00000010020268990001", 10000.0
            );
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(2L)).thenReturn(Optional.of(faseProcesso));
            when(clienteRepository.findById(4L)).thenReturn(Optional.of(cliente));

            assertThrows(TribunalInvalidoException.class, () -> createProcesso.criarProcesso(request, usuarioAutenticado));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando prioridade não for encontrada")
        void deveLancarExcecaoQuandoPrioridadeNaoEncontrada() {
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    4L, "Descrição", "Obs", 99L, 2L, null, null, 10000.0
            );
            when(prioridadeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> createProcesso.criarProcesso(request, usuarioAutenticado));
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não for encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    99L, "Descrição", "Obs", 1L, 2L, null, null, 10000.0
            );
            when(prioridadeRepository.findById(1L)).thenReturn(Optional.of(prioridade));
            when(faseProcessoRepository.findById(2L)).thenReturn(Optional.of(faseProcesso));
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> createProcesso.criarProcesso(request, usuarioAutenticado));
            verify(processoAppRepository, never()).save(any());
        }
    }
}
