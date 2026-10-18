package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NegarSolicitacao Use Case")
class NegarSolicitacaoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private StatusClienteRepository statusClienteRepository;

    @Mock
    private DeleteProcessoApp deleteProcessoApp;

    @InjectMocks
    private NegarSolicitacao negarSolicitacao;

    private ProcessoApp processoApp;
    private Cliente cliente;
    private StatusCliente statusInativo;
    private StatusCliente statusAtivo;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Cliente Teste");

        processoApp = new ProcessoApp();
        processoApp.setId(1L);
        processoApp.setCliente(cliente);
        processoApp.setStatus(StatusProcesso.SOLICITACAO);

        statusInativo = new StatusCliente();
        statusInativo.setId(2L);
        statusInativo.setNome("INATIVO");

        statusAtivo = new StatusCliente();
        statusAtivo.setId(1L);
        statusAtivo.setNome("ATIVO");
    }

    @Nested
    @DisplayName("negar")
    class NegarTest {

        @Test
        @DisplayName("Deve excluir a solicitação e inativar o cliente sem outro processo")
        void deveNegarSolicitacaoComStatusNegado() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(false);
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                    .thenReturn(Optional.of(statusInativo));

            negarSolicitacao.negar(1L);

            assertEquals(statusInativo, cliente.getStatusCliente());
            assertNotNull(cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
            verify(deleteProcessoApp).deletarProcesso(1L);
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve manter cliente ativo e a data quando existir outro processo")
        void deveManterClienteAtivoQuandoPossuirOutroProcesso() {
            LocalDateTime dataTransformacao = LocalDateTime.of(2025, 3, 1, 12, 0);
            cliente.setStatusCliente(statusAtivo);
            cliente.setDataHoraTransformacaoEmCliente(dataTransformacao);
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(true);

            negarSolicitacao.negar(1L);

            assertEquals(statusAtivo, cliente.getStatusCliente());
            assertEquals(dataTransformacao, cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository, never()).save(any());
            verify(deleteProcessoApp).deletarProcesso(1L);
        }

        @Test
        @DisplayName("Deve inativar cliente ativo sem outro processo")
        void deveInativarClienteAtivoSemOutroProcesso() {
            cliente.setStatusCliente(statusAtivo);
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(false);
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                    .thenReturn(Optional.of(statusInativo));

            negarSolicitacao.negar(1L);

            assertEquals(statusInativo, cliente.getStatusCliente());
            assertNotNull(cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve inativar cliente que não esteja ativo")
        void deveInativarClienteLead() {
            StatusCliente statusLead = new StatusCliente(3L, "LEAD");
            cliente.setStatusCliente(statusLead);
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(true);
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                    .thenReturn(Optional.of(statusInativo));

            negarSolicitacao.negar(1L);

            assertEquals(statusInativo, cliente.getStatusCliente());
            assertNotNull(cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve preservar dados oficiais compartilhados com outro processo")
        void deveDesvincularDadosOficiaisCompartilhadosAntesDeExcluirSolicitacao() {
            br.com.sgpj.entity.Processo.ProcessoApi processoApi = new br.com.sgpj.entity.Processo.ProcessoApi();
            processoApi.setId(25L);
            processoApp.setProcessoApi(processoApi);
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(processoAppRepository.existsByCliente_IdAndIdNot(10L, 1L)).thenReturn(false);
            when(processoAppRepository.existsByProcessoApi_IdAndIdNot(25L, 1L)).thenReturn(true);
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                    .thenReturn(Optional.of(statusInativo));

            negarSolicitacao.negar(1L);

            assertNull(processoApp.getProcessoApi());
            verify(deleteProcessoApp).deletarProcesso(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando solicitação não for encontrada")
        void deveLancarExcecaoQuandoSolicitacaoNaoEncontrada() {
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> negarSolicitacao.negar(99L));
            verify(clienteRepository, never()).save(any());
            verify(deleteProcessoApp, never()).deletarProcesso(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status cliente INATIVO não existir no banco")
        void deveLancarExcecaoQuandoStatusClienteNaoEncontrado() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.INATIVO.getNome()))
                    .thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> negarSolicitacao.negar(1L));
            verify(clienteRepository, never()).save(any());
            verify(deleteProcessoApp, never()).deletarProcesso(any());
        }
    }
}
