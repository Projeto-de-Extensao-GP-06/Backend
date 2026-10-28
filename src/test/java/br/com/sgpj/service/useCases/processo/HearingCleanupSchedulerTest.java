package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do HearingCleanupScheduler")
class HearingCleanupSchedulerTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

    private HearingCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new HearingCleanupScheduler(processoAppRepository, usuarioRepository, registroHistorico);
    }

    @Test
    @DisplayName("Não deve fazer nada quando não houver audiências passadas")
    void naoDeveFazerNadaQuandoNaoHouverAudienciasPassadas() {
        LocalDateTime referencia = LocalDateTime.of(2026, 10, 28, 2, 0);
        when(processoAppRepository.buscarAudienciasPassadas(StatusProcesso.ATIVO, referencia))
                .thenReturn(Collections.emptyList());

        scheduler.processarAudienciasPassadas(referencia);

        verifyNoInteractions(usuarioRepository, registroHistorico);
    }

    @Test
    @DisplayName("Deve limpar data_audiencia, atualizar ultima_atualizacao e registrar histórico com usuário SISTEMA")
    void deveLimparAudienciasPassadasERegistrarHistorico() {
        LocalDateTime referencia = LocalDateTime.of(2026, 10, 28, 2, 0);

        Usuario usuarioSistema = new Usuario();
        usuarioSistema.setId(Usuario.ID_SISTEMA);
        usuarioSistema.setNome(Usuario.NOME_SISTEMA);
        usuarioSistema.setEmail(Usuario.EMAIL_SISTEMA);

        ProcessoApp p1 = new ProcessoApp();
        p1.setId(10L);
        p1.setStatus(StatusProcesso.ATIVO);
        p1.setDataAudiencia(LocalDateTime.of(2026, 10, 15, 14, 0));

        ProcessoApp p2 = new ProcessoApp();
        p2.setId(11L);
        p2.setStatus(StatusProcesso.ATIVO);
        p2.setDataAudiencia(LocalDateTime.of(2026, 10, 27, 9, 30));

        when(processoAppRepository.buscarAudienciasPassadas(StatusProcesso.ATIVO, referencia))
                .thenReturn(List.of(p1, p2));
        when(usuarioRepository.findById(Usuario.ID_SISTEMA))
                .thenReturn(Optional.of(usuarioSistema));

        scheduler.processarAudienciasPassadas(referencia);

        assertNull(p1.getDataAudiencia());
        assertEquals(referencia, p1.getUltimaAtualizacao());
        verify(processoAppRepository).save(p1);
        verify(registroHistorico).registrar(
                p1,
                usuarioSistema,
                RegistroHistoricoProcesso.AUDIENCIA,
                "Audiência realizada em 15/10/2026 às 14:00."
        );

        assertNull(p2.getDataAudiencia());
        assertEquals(referencia, p2.getUltimaAtualizacao());
        verify(processoAppRepository).save(p2);
        verify(registroHistorico).registrar(
                p2,
                usuarioSistema,
                RegistroHistoricoProcesso.AUDIENCIA,
                "Audiência realizada em 27/10/2026 às 09:30."
        );
    }

    @Test
    @DisplayName("Deve lançar ObjetoNaoEncontradoException quando usuário SISTEMA não existir")
    void deveLancarExcecaoQuandoUsuarioSistemaNaoExistir() {
        LocalDateTime referencia = LocalDateTime.of(2026, 10, 28, 2, 0);

        ProcessoApp p1 = new ProcessoApp();
        p1.setId(10L);
        p1.setStatus(StatusProcesso.ATIVO);
        p1.setDataAudiencia(LocalDateTime.of(2026, 10, 15, 14, 0));

        when(processoAppRepository.buscarAudienciasPassadas(StatusProcesso.ATIVO, referencia))
                .thenReturn(List.of(p1));
        when(usuarioRepository.findById(Usuario.ID_SISTEMA)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(Usuario.EMAIL_SISTEMA)).thenReturn(null);

        assertThrows(
                ObjetoNaoEncontradoException.class,
                () -> scheduler.processarAudienciasPassadas(referencia)
        );
    }
}
