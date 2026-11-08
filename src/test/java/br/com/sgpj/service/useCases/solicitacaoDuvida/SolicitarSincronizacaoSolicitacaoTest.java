package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Processos.SincronizacaoProcessoResponse;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitarSincronizacaoSolicitacao — Testes do use case de sincronização de solicitação")
class SolicitarSincronizacaoSolicitacaoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private PrepararVinculoProcessoOficial prepararVinculoProcessoOficial;

    @Mock
    private SincronizacaoProcessoProducer sincronizacaoProducer;

    @InjectMocks
    private SolicitarSincronizacaoSolicitacao useCase;

    @Test
    @DisplayName("Deve solicitar sincronização com sucesso quando tribunal já vinculado")
    void deveSolicitarComSucessoQuandoTribunalJaVinculado() {
        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(30L);
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("10108901220228260292");
        processoApp.setProcessoApi(processoApi);
        SiglaTribunal tribunal = new SiglaTribunal();
        tribunal.setNome("TJSP");
        processoApp.setSiglaTribunal(tribunal);

        when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processoApp));

        SincronizacaoProcessoResponse resposta = useCase.solicitar(30L);

        assertNotNull(resposta);
        assertEquals(30L, resposta.processoId());
        assertEquals("EM_PROCESSAMENTO", resposta.status());
        verify(sincronizacaoProducer).enviarSolicitacao(30L);
        verify(processoAppRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve inferir tribunal via PrepararVinculoProcessoOficial se ausente e solicitar")
    void deveInferirTribunalSeAusente() {
        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(30L);
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("10108901220228260292");
        processoApp.setProcessoApi(processoApi);

        when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processoApp));
        when(prepararVinculoProcessoOficial.preparar(processoApp, "10108901220228260292")).thenAnswer(invocation -> {
            SiglaTribunal t = new SiglaTribunal();
            t.setNome("TJSP");
            processoApp.setSiglaTribunal(t);
            return true;
        });

        SincronizacaoProcessoResponse resposta = useCase.solicitar(30L);

        assertNotNull(resposta);
        assertEquals(30L, resposta.processoId());
        verify(processoAppRepository).save(processoApp);
        verify(sincronizacaoProducer).enviarSolicitacao(30L);
    }

    @Test
    @DisplayName("Deve lançar ObjetoNaoEncontradoException quando solicitação não existe")
    void deveLancarExcecaoQuandoNaoExiste() {
        when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjetoNaoEncontradoException.class, () -> useCase.solicitar(99L));
        verify(sincronizacaoProducer, never()).enviarSolicitacao(any());
    }

    @Test
    @DisplayName("Deve lançar OperacaoInvalidaException quando solicitação não possui CNJ")
    void deveLancarExcecaoQuandoSemCnj() {
        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(30L);

        when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processoApp));

        OperacaoInvalidaException ex = assertThrows(OperacaoInvalidaException.class, () -> useCase.solicitar(30L));
        assertTrue(ex.getMessage().contains("número CNJ vinculado"));
        verify(sincronizacaoProducer, never()).enviarSolicitacao(any());
    }

    @Test
    @DisplayName("Deve lançar OperacaoInvalidaException quando não é possível identificar tribunal")
    void deveLancarExcecaoQuandoNaoConsegueIdentificarTribunal() {
        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(30L);
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000000000000000000");
        processoApp.setProcessoApi(processoApi);

        when(processoAppRepository.findById(30L)).thenReturn(Optional.of(processoApp));
        when(prepararVinculoProcessoOficial.preparar(processoApp, "00000000000000000000")).thenReturn(false);

        OperacaoInvalidaException ex = assertThrows(OperacaoInvalidaException.class, () -> useCase.solicitar(30L));
        assertTrue(ex.getMessage().contains("identificar o tribunal"));
        verify(sincronizacaoProducer, never()).enviarSolicitacao(any());
    }
}
