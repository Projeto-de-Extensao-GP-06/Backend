package br.com.sgpj.service;

import br.com.sgpj.dto.response.PrioridadeResponse;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.repository.PrioridadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PrioridadeService")
class PrioridadeServiceTest {

    @Mock
    private PrioridadeRepository prioridadeRepository;

    private PrioridadeService service;

    @BeforeEach
    void setUp() {
        service = new PrioridadeService(prioridadeRepository);
    }

    @Test
    @DisplayName("Deve listar e mapear as prioridades")
    void deveListarEFazerMapeamentoCorreto() {
        when(prioridadeRepository.findAll()).thenReturn(List.of(
                new Prioridade(1L, "Baixa"),
                new Prioridade(2L, "Alta")
        ));

        List<PrioridadeResponse> resultado = service.listarPrioridades();

        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals("Baixa", resultado.get(0).nome()),
                () -> assertEquals(2L, resultado.get(1).id()),
                () -> assertEquals("Alta", resultado.get(1).nome())
        );

        verify(prioridadeRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver prioridades")
    void deveRetornarListaVaziaQuandoNaoHouverPrioridades() {
        when(prioridadeRepository.findAll()).thenReturn(List.of());

        List<PrioridadeResponse> resultado = service.listarPrioridades();

        assertTrue(resultado.isEmpty());
        verify(prioridadeRepository).findAll();
    }
}
