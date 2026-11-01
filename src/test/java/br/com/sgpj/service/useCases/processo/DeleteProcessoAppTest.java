package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.useCases.cliente.AtualizarStatusClientePorProcessos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteProcessoApp Use Case")
class DeleteProcessoAppTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private DeleteProcessoDocumento deleteProcessoDocumento;

    @Mock
    private DeleteHistoricoProcesso deleteHistoricoProcesso;

    @Mock
    private AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;

    @InjectMocks
    private DeleteProcessoApp deleteProcessoApp;

    private ProcessoApp processo;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Cliente Teste");

        processo = new ProcessoApp();
        processo.setId(1L);
        processo.setCliente(cliente);
    }

    @Test
    @DisplayName("Deve deletar processo com sucesso e acionar atualização do status do cliente")
    void deveDeletarProcessoEAtualizarStatusCliente() {
        when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processo));

        deleteProcessoApp.deletarProcesso(1L);

        verify(deleteProcessoDocumento).deletarVinculosPorProcessoAppId(1L);
        verify(deleteHistoricoProcesso).deletarPorProcessoApp(processo);
        verify(processoAppRepository).delete(processo);
        verify(atualizarStatusClientePorProcessos).atualizarStatusAposAlteracaoOuExclusao(cliente, 1L);
    }

    @Test
    @DisplayName("Deve deletar processo sem falhar quando cliente for nulo")
    void deveDeletarProcessoQuandoClienteNulo() {
        processo.setCliente(null);
        when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processo));

        deleteProcessoApp.deletarProcesso(1L);

        verify(deleteProcessoDocumento).deletarVinculosPorProcessoAppId(1L);
        verify(deleteHistoricoProcesso).deletarPorProcessoApp(processo);
        verify(processoAppRepository).delete(processo);
        verifyNoInteractions(atualizarStatusClientePorProcessos);
    }

    @Test
    @DisplayName("Deve lançar exceção quando processo não for encontrado")
    void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
        when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ObjetoNaoEncontradoException.class, () -> deleteProcessoApp.deletarProcesso(99L));

        verifyNoInteractions(deleteProcessoDocumento, deleteHistoricoProcesso, atualizarStatusClientePorProcessos);
        verify(processoAppRepository, never()).delete(any(ProcessoApp.class));
    }

    @Test
    @DisplayName("Deve deletar lista de processos corretamente")
    void deveDeletarListaDeProcessos() {
        ProcessoApp p1 = new ProcessoApp();
        p1.setId(1L);
        ProcessoApp p2 = new ProcessoApp();
        p2.setId(2L);
        List<ProcessoApp> lista = List.of(p1, p2);

        deleteProcessoApp.deletarProcessos(lista);

        verify(deleteProcessoDocumento).deletarVinculosPorProcessoAppId(1L);
        verify(deleteProcessoDocumento).deletarVinculosPorProcessoAppId(2L);
        verify(deleteHistoricoProcesso).deletarPorProcessoApp(p1);
        verify(deleteHistoricoProcesso).deletarPorProcessoApp(p2);
        verify(processoAppRepository).deleteAll(lista);
    }
}
