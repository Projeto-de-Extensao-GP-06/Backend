package br.com.sgpj.service;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HistoricoProcessoService")
class HistoricoProcessoServiceTest {

    @Mock
    private HistoricoProcessoRepository historicoProcessoRepository;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private TipoAlteracaoRepository tipoAlteracaoRepository;

    private HistoricoProcessoService service;

    @BeforeEach
    void setUp() {
        service = new HistoricoProcessoService(
                historicoProcessoRepository,
                processoAppRepository,
                tipoAlteracaoRepository
        );
    }

    @Nested
    @DisplayName("adicionarRegistro")
    class AdicionarRegistroTest {

        @Test
        @DisplayName("Deve registrar historico e mapear a resposta")
        void deveRegistrarHistoricoEMapearResposta() {
            ProcessoApp processo = criarProcessoApp(10L);
            Usuario usuarioAutenticado = criarUsuario(20L, "Usuario Autenticado");
            TipoAlteracao tipoAlteracao = new TipoAlteracao(30L, "ALTERACAO");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    "Mudanca de fase",
                    LocalDateTime.of(2026, 7, 10, 10, 0),
                    30L,
                    9L,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(tipoAlteracaoRepository.findById(30L)).thenReturn(Optional.of(tipoAlteracao));
            when(historicoProcessoRepository.save(any(HistoricoProcesso.class))).thenAnswer(invocation -> {
                HistoricoProcesso historico = invocation.getArgument(0);
                historico.setId(77L);
                return historico;
            });

            HistoricoProcessoResponse response = service.adicionarRegistro(request, usuarioAutenticado);

            ArgumentCaptor<HistoricoProcesso> captor = ArgumentCaptor.forClass(HistoricoProcesso.class);
            verify(historicoProcessoRepository).save(captor.capture());

            HistoricoProcesso salvo = captor.getValue();
            assertAll(
                    () -> assertEquals(10L, salvo.getProcessoApp().getId()),
                    () -> assertEquals(20L, salvo.getUsuarioUtilizado().getId()),
                    () -> assertEquals("Mudanca de fase", salvo.getDescricaoAlteracao()),
                    () -> assertEquals(tipoAlteracao, salvo.getTipoAlteracao()),
                    () -> assertNotNull(salvo.getDataHoraAlteracao()),
                    () -> assertEquals(77L, response.historicoProcessoId()),
                    () -> assertEquals(10L, response.processoAppId()),
                    () -> assertEquals(20L, response.usuarioUtilizadoId()),
                    () -> assertEquals("Usuario Autenticado", response.usuarioUtilizadoNome()),
                    () -> assertEquals("Mudanca de fase", response.descricaoAlteracao()),
                    () -> assertEquals(30L, response.tipoAlteracaoId()),
                    () -> assertEquals("ALTERACAO", response.tipoAlteracaoNome())
            );
        }

        @Test
        @DisplayName("Deve usar usuario autenticado como usuario utilizado")
        void deveUsarUsuarioAutenticadoComoUsuarioUtilizado() {
            ProcessoApp processo = criarProcessoApp(10L);
            Usuario usuarioAutenticado = criarUsuario(99L, "Usuario Autenticado");
            TipoAlteracao tipoAlteracao = new TipoAlteracao(30L, "ALTERACAO");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    "Mudanca de fase",
                    null,
                    30L,
                    null,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(tipoAlteracaoRepository.findById(30L)).thenReturn(Optional.of(tipoAlteracao));
            when(historicoProcessoRepository.save(any(HistoricoProcesso.class))).thenAnswer(invocation -> {
                HistoricoProcesso historico = invocation.getArgument(0);
                historico.setId(77L);
                return historico;
            });

            HistoricoProcessoResponse response = service.adicionarRegistro(request, usuarioAutenticado);

            ArgumentCaptor<HistoricoProcesso> captor = ArgumentCaptor.forClass(HistoricoProcesso.class);
            verify(historicoProcessoRepository).save(captor.capture());

            HistoricoProcesso salvo = captor.getValue();
            assertEquals(usuarioAutenticado, salvo.getUsuarioUtilizado());
            assertEquals(usuarioAutenticado.getId(), response.usuarioUtilizadoId());
        }

        @Test
        @DisplayName("Deve lancar excecao quando processo nao for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoForEncontrado() {
            Usuario usuarioAutenticado = criarUsuario(20L, "Usuario Autenticado");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    "Mudanca de fase",
                    null,
                    30L,
                    null,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.adicionarRegistro(request, usuarioAutenticado));
            verifyNoInteractions(tipoAlteracaoRepository, historicoProcessoRepository);
        }

        @Test
        @DisplayName("Deve lancar excecao quando tipo de alteracao nao for encontrado")
        void deveLancarExcecaoQuandoTipoAlteracaoNaoForEncontrado() {
            ProcessoApp processo = criarProcessoApp(10L);
            Usuario usuarioAutenticado = criarUsuario(20L, "Usuario Autenticado");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    "Mudanca de fase",
                    null,
                    30L,
                    null,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(tipoAlteracaoRepository.findById(30L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.adicionarRegistro(request, usuarioAutenticado));
            verify(historicoProcessoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve impedir falsificacao de usuario utilizado")
        void deveImpedirFalsificacaoDeUsuarioUtilizado() {
            ProcessoApp processo = criarProcessoApp(10L);
            Usuario usuarioAutenticado = criarUsuario(5L, "Usuario Autenticado");
            Usuario usuarioFalsificado = criarUsuario(99L, "Usuario Falsificado");
            TipoAlteracao tipoAlteracao = new TipoAlteracao(30L, "ALTERACAO");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    "Mudanca de fase",
                    null,
                    30L,
                    null,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(tipoAlteracaoRepository.findById(30L)).thenReturn(Optional.of(tipoAlteracao));
            when(historicoProcessoRepository.save(any(HistoricoProcesso.class))).thenAnswer(invocation -> {
                HistoricoProcesso historico = invocation.getArgument(0);
                historico.setId(77L);
                return historico;
            });

            HistoricoProcessoResponse response = service.adicionarRegistro(request, usuarioAutenticado);

            ArgumentCaptor<HistoricoProcesso> captor = ArgumentCaptor.forClass(HistoricoProcesso.class);
            verify(historicoProcessoRepository).save(captor.capture());

            HistoricoProcesso salvo = captor.getValue();
            assertNotEquals(usuarioFalsificado, salvo.getUsuarioUtilizado());
            assertEquals(usuarioAutenticado, salvo.getUsuarioUtilizado());
            assertEquals(usuarioAutenticado.getId(), response.usuarioUtilizadoId());
            assertNotEquals(usuarioFalsificado.getId(), response.usuarioUtilizadoId());
        }

        @Test
        @DisplayName("Deve registrar historico sem descricao alteracao")
        void deveRegistrarHistoricoSemDescricaoAlteracao() {
            ProcessoApp processo = criarProcessoApp(10L);
            Usuario usuarioAutenticado = criarUsuario(20L, "Usuario Autenticado");
            TipoAlteracao tipoAlteracao = new TipoAlteracao(30L, "ALTERACAO");
            HistoricoProcessoRequest request = new HistoricoProcessoRequest(
                    10L,
                    null,
                    null,
                    30L,
                    null,
                    11L
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(tipoAlteracaoRepository.findById(30L)).thenReturn(Optional.of(tipoAlteracao));
            when(historicoProcessoRepository.save(any(HistoricoProcesso.class))).thenAnswer(invocation -> {
                HistoricoProcesso historico = invocation.getArgument(0);
                historico.setId(77L);
                return historico;
            });

            HistoricoProcessoResponse response = service.adicionarRegistro(request, usuarioAutenticado);

            ArgumentCaptor<HistoricoProcesso> captor = ArgumentCaptor.forClass(HistoricoProcesso.class);
            verify(historicoProcessoRepository).save(captor.capture());

            HistoricoProcesso salvo = captor.getValue();
            assertEquals(usuarioAutenticado, salvo.getUsuarioUtilizado());
            assertNull(salvo.getDescricaoAlteracao());
            assertNotNull(salvo.getDataHoraAlteracao());
        }
    }

    @Nested
    @DisplayName("buscarPorIdProcesso")
    class BuscarPorIdProcessoTest {

        @Test
        @DisplayName("Deve retornar o historico do processo")
        void deveRetornarHistoricoDoProcesso() {
            ProcessoApp processo = criarProcessoApp(10L);
            HistoricoProcesso historico = criarHistoricoProcesso(
                    90L,
                    processo,
                    criarUsuario(20L, "Usuario Historico"),
                    new TipoAlteracao(30L, "ALTERACAO")
            );

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(historicoProcessoRepository.findByProcessoApp(processo)).thenReturn(List.of(historico));

            List<HistoricoProcessoResponse> resultado = service.buscarPorIdProcesso(10L);

            assertAll(
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(90L, resultado.get(0).historicoProcessoId()),
                    () -> assertEquals(10L, resultado.get(0).processoAppId()),
                    () -> assertEquals(20L, resultado.get(0).usuarioUtilizadoId()),
                    () -> assertEquals("Usuario Historico", resultado.get(0).usuarioUtilizadoNome()),
                    () -> assertEquals("Mudanca de fase", resultado.get(0).descricaoAlteracao()),
                    () -> assertEquals(30L, resultado.get(0).tipoAlteracaoId()),
                    () -> assertEquals("ALTERACAO", resultado.get(0).tipoAlteracaoNome())
            );
        }

        @Test
        @DisplayName("Deve lancar excecao quando processo nao for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoForEncontrado() {
            when(processoAppRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.buscarPorIdProcesso(10L));
            verifyNoInteractions(historicoProcessoRepository);
        }
    }

    @Nested
    @DisplayName("deletarPorIdProcesso")
    class DeletarPorIdProcessoTest {

        @Test
        @DisplayName("Deve remover os registros do historico quando o processo existir")
        void deveRemoverHistoricoQuandoProcessoExistir() {
            ProcessoApp processo = criarProcessoApp(10L);
            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));

            service.deletarPorIdProcesso(10L);

            verify(historicoProcessoRepository).deleteByProcessoApp(processo);
        }

        @Test
        @DisplayName("Deve lancar excecao quando processo nao for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoForEncontrado() {
            when(processoAppRepository.findById(10L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> service.deletarPorIdProcesso(10L));
            verifyNoInteractions(historicoProcessoRepository);
        }
    }

    private ProcessoApp criarProcessoApp(Long id) {
        ProcessoApp processo = new ProcessoApp();
        processo.setId(id);
        return processo;
    }

    private Usuario criarUsuario(Long id, String nome) {
        return new Usuario(
                id,
                nome,
                nome.toLowerCase().replace(" ", ".") + "@email.com",
                null,
                null,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
        );
    }

    private HistoricoProcesso criarHistoricoProcesso(
            Long id,
            ProcessoApp processoApp,
            Usuario usuario,
            TipoAlteracao tipoAlteracao
    ) {
        HistoricoProcesso historico = new HistoricoProcesso();
        historico.setId(id);
        historico.setProcessoApp(processoApp);
        historico.setUsuarioUtilizado(usuario);
        historico.setDescricaoAlteracao("Mudanca de fase");
        historico.setDataHoraAlteracao(LocalDateTime.of(2026, 7, 10, 11, 0));
        historico.setTipoAlteracao(tipoAlteracao);
        return historico;
    }
}
