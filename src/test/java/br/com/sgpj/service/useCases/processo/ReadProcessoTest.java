package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.security.AuthorizationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReadProcesso Use Case")
class ReadProcessoTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private HistoricoProcessoRepository historicoProcessoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ReadProcesso readProcesso;

    private Cliente cliente;
    private ProcessoApp processoAtivo;
    private ProcessoApp processoSuspenso;
    private ProcessoApp processoEncerrado;
    private ProcessoApp processoNegado;
    private ProcessoApp processoSolicitacao;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Cliente Teste");

        processoAtivo = criarProcesso(1L, StatusProcesso.ATIVO);
        processoSuspenso = criarProcesso(2L, StatusProcesso.SUSPENSO);
        processoEncerrado = criarProcesso(3L, StatusProcesso.ENCERRADO);
        processoNegado = criarProcesso(4L, StatusProcesso.NEGADO);
        processoSolicitacao = criarProcesso(5L, StatusProcesso.SOLICITACAO);
    }

    @Nested
    @DisplayName("listarProcessos")
    class ListarProcessosTest {

        @Test
        @DisplayName("Deve listar todos os processos mapeados para mini response")
        void deveListarProcessosIgnorandoSolicitacao() {
            PageRequest pageable = PageRequest.of(0, 20);
            when(processoAppRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(
                    processoAtivo,
                    processoSuspenso,
                    processoEncerrado,
                    processoNegado
            ), pageable, 4));

            Page<ProcessoAppMiniResponse> resultado = readProcesso.listarProcessos(pageable, null, null, null, null, null, null);

            assertNotNull(resultado);
            assertEquals(4, resultado.getContent().size());
            verify(processoAppRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando apenas existirem solicitações")
        void deveRetornarListaVaziaQuandoApenasSolicitacao() {
            PageRequest pageable = PageRequest.of(0, 20);
            when(processoAppRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(), pageable, 0));

            Page<ProcessoAppMiniResponse> resultado = readProcesso.listarProcessos(pageable, null, null, null, null, null, null);

            assertNotNull(resultado);
            assertTrue(resultado.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorIdTest {

        @Test
        @DisplayName("Deve buscar processo por ID com sucesso e incluir histórico")
        void deveBuscarPorIdComSucesso() {
            when(processoAppRepository.findById(1L)).thenReturn(Optional.of(processoAtivo));
            when(historicoProcessoRepository.findByProcessoApp(processoAtivo)).thenReturn(List.of());

            ProcessoAppResponse resultado = readProcesso.buscarPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.id());
            assertEquals(StatusProcesso.ATIVO, resultado.status());
            verify(processoAppRepository).findById(1L);
            verify(historicoProcessoRepository).findByProcessoApp(processoAtivo);
        }

        @Test
        @DisplayName("Deve lançar exceção quando processo tiver status SOLICITACAO")
        void deveLancarExcecaoQuandoProcessoForSolicitacao() {
            when(processoAppRepository.findById(5L)).thenReturn(Optional.of(processoSolicitacao));

            ObjetoNaoEncontradoException exception = assertThrows(ObjetoNaoEncontradoException.class,
                    () -> readProcesso.buscarPorId(5L));

            assertEquals("Processo não encontrado para o ID: 5", exception.getMessage());
            verifyNoInteractions(historicoProcessoRepository);
        }

        @Test
        @DisplayName("Deve lançar exceção quando processo não for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> readProcesso.buscarPorId(99L));
        }
    }

    @Nested
    @DisplayName("buscarPorClienteId")
    class BuscarPorClienteIdTest {

        @Test
        @DisplayName("Deve retornar processos do cliente excluindo solicitações")
        void deveRetornarProcessosDoClienteExcluindoSolicitacao() {
            when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
            when(processoAppRepository.findByCliente(cliente)).thenReturn(List.of(
                    processoAtivo,
                    processoSuspenso,
                    processoSolicitacao
            ));

            List<ProcessoAppMiniResponse> resultado = readProcesso.buscarPorClienteId(10L);

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            verify(authorizationUtil).verificarAcessoCliente(10L);
            verify(processoAppRepository).findByCliente(cliente);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não for encontrado")
        void deveLancarExcecaoQuandoClienteNaoEncontrado() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ObjetoNaoEncontradoException.class, () -> readProcesso.buscarPorClienteId(99L));
            verify(authorizationUtil).verificarAcessoCliente(99L);
        }
    }

    @Nested
    @DisplayName("buscaFiltro")
    class BuscaFiltroTest {

        @Test
        @DisplayName("Deve normalizar query e retornar processos filtrados excluindo solicitações")
        void deveFiltrarProcessosExcluindoSolicitacao() {
            when(processoAppRepository.buscaFiltro("contrato", 1L, 2L))
                    .thenReturn(List.of(processoAtivo, processoSolicitacao));

            List<ProcessoAppMiniResponse> resultado = readProcesso.buscaFiltro("  contrato  ", 1L, 2L);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(1L, resultado.getFirst().id());
            verify(processoAppRepository).buscaFiltro("contrato", 1L, 2L);
        }

        @Test
        @DisplayName("Deve tratar query em branco ou nula como null na busca")
        void deveTratarQueryEmBrancoComoNull() {
            when(processoAppRepository.buscaFiltro(null, null, null))
                    .thenReturn(List.of(processoEncerrado));

            List<ProcessoAppMiniResponse> resultado = readProcesso.buscaFiltro("   ", null, null);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(processoAppRepository).buscaFiltro(null, null, null);
        }
    }

    private ProcessoApp criarProcesso(Long id, StatusProcesso status) {
        ProcessoApp processo = new ProcessoApp();
        processo.setId(id);
        processo.setCliente(cliente);
        processo.setStatus(status);
        processo.setDescricaoCaso("Caso " + id);
        processo.setPrioridade(new Prioridade(1L, "NORMAL"));
        processo.setFaseProcesso(new FaseProcesso(1L, "INICIAL"));
        processo.setSiglaTribunal(new SiglaTribunal(1L, "TJSP"));
        processo.setDataHoraInicio(LocalDateTime.now());
        processo.setUltimaAtualizacao(LocalDateTime.now());
        return processo;
    }
}
