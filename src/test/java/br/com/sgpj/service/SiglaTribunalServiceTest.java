package br.com.sgpj.service;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.repository.SiglaTribunalRepository;
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
@DisplayName("SiglaTribunalService")
class SiglaTribunalServiceTest {

    @Mock
    private SiglaTribunalRepository siglaTribunalRepository;

    private SiglaTribunalService service;

    @BeforeEach
    void setUp() {
        service = new SiglaTribunalService(siglaTribunalRepository);
    }

    @Test
    @DisplayName("Deve listar e mapear os tribunais")
    void deveListarEFazerMapeamentoCorreto() {
        when(siglaTribunalRepository.findAll()).thenReturn(List.of(
                new SiglaTribunal(1L, "TJSP"),
                new SiglaTribunal(2L, "TRF3")
        ));

        List<SiglaTribunalResponse> resultado = service.listarTribunais();

        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).id()),
                () -> assertEquals("TJSP", resultado.get(0).siglaTribunal()),
                () -> assertEquals(2L, resultado.get(1).id()),
                () -> assertEquals("TRF3", resultado.get(1).siglaTribunal())
        );

        verify(siglaTribunalRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver tribunais")
    void deveRetornarListaVaziaQuandoNaoHouverTribunais() {
        when(siglaTribunalRepository.findAll()).thenReturn(List.of());

        List<SiglaTribunalResponse> resultado = service.listarTribunais();

        assertTrue(resultado.isEmpty());
        verify(siglaTribunalRepository).findAll();
    }
}
