package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AceitarSolicitacao Use Case")
class AceitarSolicitacaoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private StatusClienteRepository statusClienteRepository;

    @InjectMocks
    private AceitarSolicitacao aceitarSolicitacao;

    private ProcessoApp processoApp;
    private Cliente cliente;
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
        processoApp.setDescricaoCaso("Descrição dúvida");

        statusAtivo = new StatusCliente();
        statusAtivo.setId(1L);
        statusAtivo.setNome("ATIVO");
    }

    @Nested
    @DisplayName("aceitar")
    class AceitarTest {

        @Test
        @DisplayName("Deve aceitar solicitação, mudar status do processo para ATIVO e cliente para ATIVO")
        void deveAceitarSolicitacaoComStatusAtivo() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.of(statusAtivo));

            SolicitacaoClientePreenchimentoResponse response = aceitarSolicitacao.aceitar(1L);

            ArgumentCaptor<ProcessoApp> processoCaptor = ArgumentCaptor.forClass(ProcessoApp.class);
            verify(processoAppRepository).save(processoCaptor.capture());

            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());

            assertEquals(StatusProcesso.ATIVO, processoCaptor.getValue().getStatus());
            assertNotNull(processoCaptor.getValue().getUltimaAtualizacao());
            assertEquals(statusAtivo, clienteCaptor.getValue().getStatusCliente());
            assertNotNull(clienteCaptor.getValue().getDataHoraTransformacaoEmCliente());
            assertNotNull(response);
        }

        @Test
        @DisplayName("Deve lançar exceção quando solicitação não for encontrada")
        void deveLancarExcecaoQuandoSolicitacaoNaoEncontrada() {
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> aceitarSolicitacao.aceitar(99L));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando status cliente ATIVO não existir no banco")
        void deveLancarExcecaoQuandoStatusClienteNaoEncontrado() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoApp));
            when(statusClienteRepository.findByNomeIgnoreCase(StatusClienteEnum.ATIVO.getNome()))
                    .thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> aceitarSolicitacao.aceitar(1L));
            verify(clienteRepository, never()).save(any());
            verify(processoAppRepository, never()).save(any());
        }
    }
}
