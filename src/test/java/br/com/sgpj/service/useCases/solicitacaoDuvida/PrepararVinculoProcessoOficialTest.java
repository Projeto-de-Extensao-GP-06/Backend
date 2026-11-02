package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.repository.SiglaTribunalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrepararVinculoProcessoOficialTest {

    @Mock private SiglaTribunalRepository tribunalRepository;
    private PrepararVinculoProcessoOficial preparador;
    private ProcessoApp processo;

    @BeforeEach
    void preparar() {
        preparador = new PrepararVinculoProcessoOficial(tribunalRepository);
        processo = new ProcessoApp();
    }

    @Test
    void devePrepararVinculoMinimoComCnjNormalizado() {
        SiglaTribunal tribunal = new SiglaTribunal(1L, "TJSP");
        when(tribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.of(tribunal));

        assertTrue(preparador.preparar(processo, "0000001-45.2026.8.26.0001"));

        assertEquals("00000014520268260001", processo.getProcessoApi().getNumeroCnj());
        assertEquals("TJSP", processo.getProcessoApi().getTribunalTramitacao());
        assertSame(tribunal, processo.getSiglaTribunal());
    }

    @Test
    void deveCompletarTribunalSemSubstituirDadosExistentes() {
        ProcessoApi api = new ProcessoApi();
        api.setNumeroCnj("00000014520268260001");
        api.setTribunalTramitacao("Tribunal original");
        api.setClasseProcessual("Classe original");
        processo.setProcessoApi(api);
        when(tribunalRepository.findByNomeIgnoreCase("TJSP"))
                .thenReturn(Optional.of(new SiglaTribunal(1L, "TJSP")));

        assertTrue(preparador.preparar(processo, api.getNumeroCnj()));

        assertSame(api, processo.getProcessoApi());
        assertEquals("Tribunal original", api.getTribunalTramitacao());
        assertEquals("Classe original", api.getClasseProcessual());
    }

    @Test
    void deveReutilizarTribunalVinculadoECompletarCnjAusente() {
        processo.setSiglaTribunal(new SiglaTribunal(1L, "TJSP"));
        ProcessoApi api = new ProcessoApi();
        processo.setProcessoApi(api);

        assertTrue(preparador.preparar(processo, "00000014520268260001"));

        assertSame(api, processo.getProcessoApi());
        assertEquals("00000014520268260001", api.getNumeroCnj());
        verifyNoInteractions(tribunalRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "000000145202682600011"})
    void naoDevePrepararCnjComTamanhoInvalido(String cnj) {
        assertFalse(preparador.preparar(processo, cnj));
        assertNull(processo.getProcessoApi());
        verifyNoInteractions(tribunalRepository);
    }

    @Test
    void naoDevePrepararTribunalNaoReconhecido() {
        assertFalse(preparador.preparar(processo, "00000014520268990001"));
        assertNull(processo.getProcessoApi());
        verifyNoInteractions(tribunalRepository);
    }

    @Test
    void naoDevePrepararTribunalNaoCadastrado() {
        when(tribunalRepository.findByNomeIgnoreCase("TJSP")).thenReturn(Optional.empty());
        assertFalse(preparador.preparar(processo, "00000014520268260001"));
        assertNull(processo.getProcessoApi());
    }

    @Test
    void deveTratarFalhaDeAcessoAoTribunal() {
        when(tribunalRepository.findByNomeIgnoreCase("TJSP"))
                .thenThrow(new DataAccessResourceFailureException("Sem conexão"));
        assertFalse(preparador.preparar(processo, "00000014520268260001"));
        assertNull(processo.getProcessoApi());
    }
}
