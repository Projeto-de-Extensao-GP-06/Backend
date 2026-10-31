package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Cliente;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarStatusClientePorProcessos Use Case")
class AtualizarStatusClientePorProcessosTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private StatusClienteRepository statusClienteRepository;

    @InjectMocks
    private AtualizarStatusClientePorProcessos atualizarStatusClientePorProcessos;

    private Cliente cliente;
    private StatusCliente statusAtivo;
    private StatusCliente statusInativo;
    private StatusCliente statusLead;

    @BeforeEach
    void setUp() {
        statusAtivo = new StatusCliente(1L, "ATIVO");
        statusInativo = new StatusCliente(2L, "INATIVO");
        statusLead = new StatusCliente(3L, "LEAD");

        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Cliente Teste");
    }

    @Nested
    @DisplayName("ativarCliente")
    class AtivarClienteTest {

        @Test
        @DisplayName("Deve manter cliente como ATIVO sem salvar novamente se já estiver ativo")
        void deveManterClienteAtivoSemSalvarSeJaAtivo() {
            cliente.setStatusCliente(statusAtivo);

            atualizarStatusClientePorProcessos.ativarCliente(cliente);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            verify(statusClienteRepository, never()).findByNomeIgnoreCase(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve transformar cliente INATIVO em ATIVO e definir data de transformação")
        void deveAtivarClienteInativo() {
            cliente.setStatusCliente(statusInativo);
            when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(statusAtivo));

            atualizarStatusClientePorProcessos.ativarCliente(cliente);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            assertNotNull(cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve transformar cliente LEAD em ATIVO e definir data de transformação")
        void deveAtivarClienteLead() {
            cliente.setStatusCliente(statusLead);
            when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(statusAtivo));

            atualizarStatusClientePorProcessos.ativarCliente(cliente);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            assertNotNull(cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve preservar data de transformação existente quando cliente já a possui")
        void devePreservarDataDeTransformacaoExistente() {
            LocalDateTime dataOriginal = LocalDateTime.of(2025, 1, 15, 10, 0);
            cliente.setStatusCliente(statusInativo);
            cliente.setDataHoraTransformacaoEmCliente(dataOriginal);
            when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(statusAtivo));

            atualizarStatusClientePorProcessos.ativarCliente(cliente);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            assertEquals(dataOriginal, cliente.getDataHoraTransformacaoEmCliente());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Não deve fazer nada se o cliente for nulo")
        void deveIgnorarClienteNulo() {
            assertDoesNotThrow(() -> atualizarStatusClientePorProcessos.ativarCliente(null));
            verifyNoInteractions(statusClienteRepository, clienteRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção se status ATIVO não existir no banco")
        void deveLancarExcecaoQuandoStatusNaoEncontrado() {
            cliente.setStatusCliente(statusInativo);
            when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> atualizarStatusClientePorProcessos.ativarCliente(cliente));
            verify(clienteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("atualizarStatusAposAlteracaoOuExclusao")
    class AtualizarAposAlteracaoOuExclusaoTest {

        @Test
        @DisplayName("Deve manter cliente como ATIVO se possuir outro processo ativo")
        void deveManterAtivoSePossuirOutroProcessoAtivo() {
            cliente.setStatusCliente(statusAtivo);
            when(processoAppRepository.existsByCliente_IdAndStatusAndIdNot(10L, StatusProcesso.ATIVO, 100L))
                    .thenReturn(true);

            atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, 100L);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            verify(statusClienteRepository, never()).findByNomeIgnoreCase(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve inativar cliente ATIVO se não possuir nenhum outro processo ativo")
        void deveInativarClienteSemOutroProcessoAtivo() {
            cliente.setStatusCliente(statusAtivo);
            when(processoAppRepository.existsByCliente_IdAndStatusAndIdNot(10L, StatusProcesso.ATIVO, 100L))
                    .thenReturn(false);
            when(statusClienteRepository.findByNomeIgnoreCase("INATIVO")).thenReturn(Optional.of(statusInativo));

            atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, 100L);

            assertEquals("INATIVO", cliente.getStatusCliente().getNome());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve manter cliente como INATIVO sem salvar se já estiver inativo e não possuir processos ativos")
        void deveManterInativoSemSalvarSeJaInativo() {
            cliente.setStatusCliente(statusInativo);
            when(processoAppRepository.existsByCliente_IdAndStatusAndIdNot(10L, StatusProcesso.ATIVO, 100L))
                    .thenReturn(false);

            atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, 100L);

            assertEquals("INATIVO", cliente.getStatusCliente().getNome());
            verify(statusClienteRepository, never()).findByNomeIgnoreCase(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Não deve fazer nada se cliente for nulo ou sem ID")
        void deveIgnorarClienteSemId() {
            cliente.setId(null);
            atualizarStatusClientePorProcessos.atualizarStatusAposAlteracaoOuExclusao(cliente, 100L);
            verifyNoInteractions(processoAppRepository, statusClienteRepository, clienteRepository);
        }
    }

    @Nested
    @DisplayName("sincronizarStatusCliente")
    class SincronizarStatusClienteTest {

        @Test
        @DisplayName("Deve ativar cliente se possuir processo ativo no banco")
        void deveAtivarSePossuirProcessoAtivo() {
            cliente.setStatusCliente(statusInativo);
            when(processoAppRepository.existsByCliente_IdAndStatus(10L, StatusProcesso.ATIVO)).thenReturn(true);
            when(statusClienteRepository.findByNomeIgnoreCase("ATIVO")).thenReturn(Optional.of(statusAtivo));

            atualizarStatusClientePorProcessos.sincronizarStatusCliente(cliente);

            assertEquals("ATIVO", cliente.getStatusCliente().getNome());
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("Deve inativar cliente se não possuir processo ativo no banco")
        void deveInativarSeNaoPossuirProcessoAtivo() {
            cliente.setStatusCliente(statusAtivo);
            when(processoAppRepository.existsByCliente_IdAndStatus(10L, StatusProcesso.ATIVO)).thenReturn(false);
            when(statusClienteRepository.findByNomeIgnoreCase("INATIVO")).thenReturn(Optional.of(statusInativo));

            atualizarStatusClientePorProcessos.sincronizarStatusCliente(cliente);

            assertEquals("INATIVO", cliente.getStatusCliente().getNome());
            verify(clienteRepository).save(cliente);
        }
    }
}
