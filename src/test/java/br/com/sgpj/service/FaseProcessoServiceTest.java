package br.com.sgpj.service;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.repository.FaseProcessoRepository;
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
@DisplayName("FaseProcessoService")
class FaseProcessoServiceTest {

    @Mock
    private FaseProcessoRepository faseProcessoRepository;

    private FaseProcessoService service;

    @BeforeEach
    void setUp() {
        service = new FaseProcessoService(faseProcessoRepository);
    }

    @Test
    @DisplayName("Deve listar e mapear as fases de processo")
    void deveListarEFazerMapeamentoCorreto() {
        when(faseProcessoRepository.findAll()).thenReturn(List.of(
                new FaseProcesso(1L, "Inicial"),
                new FaseProcesso(2L, "Em andamento")
        ));

        List<FaseProcessoResponse> resultado = service.listarFases();

        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals("Inicial", resultado.get(0).nome()),
                () -> assertEquals(2L, resultado.get(1).id()),
                () -> assertEquals("Em andamento", resultado.get(1).nome())
        );

        verify(faseProcessoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver fases")
    void deveRetornarListaVaziaQuandoNaoHouverFases() {
        when(faseProcessoRepository.findAll()).thenReturn(List.of());

        List<FaseProcessoResponse> resultado = service.listarFases();

        assertTrue(resultado.isEmpty());
        verify(faseProcessoRepository).findAll();
    }
}
