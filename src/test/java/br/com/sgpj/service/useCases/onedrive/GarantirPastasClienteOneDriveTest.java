package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarantirPastasClienteOneDriveTest {

    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @Mock private ClienteCaminhoArmazenamento registrador;
    @Mock private OneDriveGraphGateway graphGateway;
    @InjectMocks private GarantirPastasClienteOneDrive garantirPastas;

    @Test
    void deveUsarCaminhoSalvoMesmoComNomeDiferente() {
        when(caminhoRepository.findByClienteId(10L)).thenReturn(List.of(caminho("nome-antigo")));

        garantirPastas.garantir(cliente(), 1L);

        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", "nome-antigo");
        verifyNoInteractions(registrador);
    }

    @Test
    void deveRegistrarCaminhoAusenteAntesDeCriar() {
        when(caminhoRepository.findByClienteId(10L)).thenReturn(List.of()).thenReturn(List.of(caminho("nome-atual")));

        Cliente cliente = cliente();
        garantirPastas.garantir(cliente, 1L);

        var ordem = inOrder(registrador, graphGateway);
        ordem.verify(registrador).registrar(cliente);
        ordem.verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", "nome-atual");
    }

    @Test
    void naoDeveUsarCaminhoDeOutroClienteQuandoCadastroNaoGerarVinculo() {
        when(caminhoRepository.findByClienteId(10L)).thenReturn(List.of());

        garantirPastas.garantir(cliente(), 1L);

        verify(registrador).registrar(any());
        verifyNoInteractions(graphGateway);
    }

    @Test
    void deveGarantirTodasAsBasesSemDuplicarIgnorandoSubpastasEContinuandoAposFalha() {
        when(caminhoRepository.findByClienteId(10L)).thenReturn(List.of(
                caminho("base-um"), caminho("base-um"), caminho("base-um/processo"),
                caminho("base-dois"), caminho("base-dois\\processo")));
        when(graphGateway.garantirPasta("root:/sgpj/usuarios:", "base-um"))
                .thenThrow(new OneDriveErroException("Sem conexao"));

        garantirPastas.garantir(cliente(), 1L);

        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", "base-um");
        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", "base-dois");
        verifyNoMoreInteractions(graphGateway);
    }

    @Test
    void devePropagarFalhaDeBancoSemTentarOneDrive() {
        when(caminhoRepository.findByClienteId(10L)).thenReturn(List.of());
        doThrow(new DataIntegrityViolationException("Falha no banco")).when(registrador).registrar(any());

        assertThrows(DataIntegrityViolationException.class, () -> garantirPastas.garantir(cliente(), 1L));
        verifyNoInteractions(graphGateway);
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Nome Atual");
        return cliente;
    }

    private CaminhoPadraoDeArmazenamento caminho(String nome) {
        return new CaminhoPadraoDeArmazenamento(null, cliente(), nome, List.of());
    }
}
