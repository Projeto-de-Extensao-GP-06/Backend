package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarCaminhoArmazenamentoClienteTest {

    @Mock
    private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;

    @InjectMocks
    private RegistrarCaminhoArmazenamentoCliente registrarCaminho;

    @Test
    void deveSalvarCaminhoNormalizadoVinculadoAoCliente() {
        Cliente cliente = cliente();

        registrarCaminho.registrar(cliente);

        ArgumentCaptor<CaminhoPadraoDeArmazenamento> captor =
                ArgumentCaptor.forClass(CaminhoPadraoDeArmazenamento.class);
        verify(caminhoRepository).existsByCaminho("cliente-de-teste");
        verify(caminhoRepository).save(captor.capture());
        assertEquals("cliente-de-teste", captor.getValue().getCaminho());
        assertSame(cliente, captor.getValue().getCliente());
    }

    @Test
    void naoDeveDuplicarCaminhoExistente() {
        when(caminhoRepository.existsByCaminho("cliente-de-teste")).thenReturn(true);

        registrarCaminho.registrar(cliente());

        verify(caminhoRepository, never()).save(any());
    }

    @Test
    void devePropagarErroDePersistencia() {
        DataIntegrityViolationException erro = new DataIntegrityViolationException("Falha ao salvar caminho");
        when(caminhoRepository.save(any())).thenThrow(erro);

        assertSame(erro, assertThrows(DataIntegrityViolationException.class,
                () -> registrarCaminho.registrar(cliente())));
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("  Cliente   de Teste  ");
        return cliente;
    }
}
