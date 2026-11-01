package br.com.sgpj.service;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoDocumentoResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import br.com.sgpj.service.useCases.processo.CreateProcesso;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import br.com.sgpj.service.useCases.processo.DeleteProcessoDocumento;
import br.com.sgpj.service.useCases.processo.ReadProcesso;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService - Delegações e Operações")
class ProcessoServiceTest {

    @Mock
    private CreateProcesso createProcesso;

    @Mock
    private UpdateProcesso updateProcesso;

    @Mock
    private ReadProcesso readProcesso;

    @Mock
    private DeleteProcessoApp deleteProcessoApp;

    @Mock
    private DeleteProcessoDocumento deleteProcessoDocumento;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ProcessoDocumentoRepository processoDocumentoRepository;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private RegistroHistoricoProcesso registroHistorico;

    @Mock
    private SincronizacaoProcessoProducer sincronizacaoProcessoProducer;

    private ProcessoService processoService;

    @BeforeEach
    void setUp() {
        processoService = new ProcessoService(
                createProcesso,
                usuarioRepository,
                processoAppRepository,
                updateProcesso,
                readProcesso,
                documentoRepository,
                processoDocumentoRepository,
                authorizationUtil,
                deleteProcessoApp,
                deleteProcessoDocumento,
                registroHistorico,
                sincronizacaoProcessoProducer
        );
    }

    @Nested
    @DisplayName("Criação de Processo")
    class CriacaoProcessoTest {

        @Test
        @DisplayName("Deve delegar criação para CreateProcesso")
        void deveDelegarCriacao() {
            ProcessoAppCriacaoRequest request = mock(ProcessoAppCriacaoRequest.class);
            Usuario usuario = new Usuario();
            ProcessoAppCreateResponse response = mock(ProcessoAppCreateResponse.class);

            when(createProcesso.criarProcesso(request, usuario)).thenReturn(response);

            ProcessoAppCreateResponse resultado = processoService.criarProcesso(request, usuario);

            assertEquals(response, resultado);
            verify(createProcesso).criarProcesso(request, usuario);
        }
    }

    @Nested
    @DisplayName("Leitura de Processos")
    class LeituraProcessosTest {

        @Test
        @DisplayName("Deve delegar listagem geral para ReadProcesso")
        void deveDelegarListagem() {
            Page<ProcessoAppMiniResponse> lista = new PageImpl<>(List.of(mock(ProcessoAppMiniResponse.class)));
            PageRequest pageable = PageRequest.of(0, 20);
            when(readProcesso.listarProcessos(pageable, null, null, null, null, null, null)).thenReturn(lista);

            Page<ProcessoAppMiniResponse> resultado = processoService.listarProcessos(pageable, null, null, null, null, null, null);

            assertEquals(lista, resultado);
            verify(readProcesso).listarProcessos(pageable, null, null, null, null, null, null);
        }

        @Test
        @DisplayName("Deve delegar busca por ID para ReadProcesso")
        void deveDelegarBuscaPorId() {
            ProcessoAppResponse response = mock(ProcessoAppResponse.class);
            when(readProcesso.buscarPorId(1L)).thenReturn(response);

            ProcessoAppResponse resultado = processoService.buscarPorId(1L);

            assertEquals(response, resultado);
            verify(readProcesso).buscarPorId(1L);
        }

        @Test
        @DisplayName("Deve delegar busca por clienteId para ReadProcesso")
        void deveDelegarBuscaPorClienteId() {
            List<ProcessoAppMiniResponse> lista = List.of(mock(ProcessoAppMiniResponse.class));
            when(readProcesso.buscarPorClienteId(2L)).thenReturn(lista);

            List<ProcessoAppMiniResponse> resultado = processoService.buscarPorClienteId(2L);

            assertEquals(lista, resultado);
            verify(readProcesso).buscarPorClienteId(2L);
        }

        @Test
        @DisplayName("Deve delegar busca com filtro para ReadProcesso")
        void deveDelegarBuscaFiltro() {
            List<ProcessoAppMiniResponse> lista = List.of(mock(ProcessoAppMiniResponse.class));
            when(readProcesso.buscaFiltro("query", 1L, 2L)).thenReturn(lista);

            List<ProcessoAppMiniResponse> resultado = processoService.buscaFiltro("query", 1L, 2L);

            assertEquals(lista, resultado);
            verify(readProcesso).buscaFiltro("query", 1L, 2L);
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class AtualizacaoProcessoTest {

        @Test
        @DisplayName("Deve delegar atualização para UpdateProcesso")
        void deveDelegarAtualizacao() {
            ProcessoAppUpdateRequest request = mock(ProcessoAppUpdateRequest.class);
            ProcessoAppUpdateResponse response = mock(ProcessoAppUpdateResponse.class);
            when(updateProcesso.atualizarProcesso(1L, request)).thenReturn(response);

            ProcessoAppUpdateResponse resultado = processoService.atualizarProcesso(1L, request);

            assertEquals(response, resultado);
            verify(updateProcesso).atualizarProcesso(1L, request);
        }
    }

    @Nested
    @DisplayName("Exclusão de Processo")
    class ExclusaoProcessoTest {

        @Test
        @DisplayName("Deve delegar exclusão para DeleteProcessoApp")
        void deveDelegarExclusao() {
            processoService.deletarProcesso(1L);

            verify(deleteProcessoApp).deletarProcesso(1L);
        }
    }

    @Nested
    @DisplayName("Operações de Documentos")
    class OperacoesDocumentosTest {

        @Test
        @DisplayName("Deve anexar documento ao processo")
        void deveAnexarDocumentoAoProcesso() {
            ProcessoApp processo = new ProcessoApp();
            processo.setId(10L);
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            processo.setCliente(cliente);

            TipoDocumento tipo = new TipoDocumento();
            tipo.setId(1L);
            tipo.setNome("CONTRATO");

            Documento documento = new Documento();
            documento.setId(20L);
            documento.setNomeOriginal("contrato.pdf");
            documento.setTipoDocumento(tipo);

            Usuario usuario = new Usuario();
            usuario.setId(30L);
            usuario.setNome("Advogado");

            ProcessoDocumento processoDocSalvo = new ProcessoDocumento();
            processoDocSalvo.setId(new ProcessoDocumentoId(10L, 20L));
            processoDocSalvo.setProcessoApp(processo);
            processoDocSalvo.setDocumento(documento);
            processoDocSalvo.setAnexadoPorUsuario(usuario);
            processoDocSalvo.setAnexadoEm(LocalDateTime.now());

            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));
            when(documentoRepository.findById(20L)).thenReturn(Optional.of(documento));
            when(usuarioRepository.existsById(30L)).thenReturn(true);
            when(usuarioRepository.getReferenceById(30L)).thenReturn(usuario);
            when(processoDocumentoRepository.existsById(any(ProcessoDocumentoId.class))).thenReturn(false);
            when(processoDocumentoRepository.save(any(ProcessoDocumento.class))).thenReturn(processoDocSalvo);

            ProcessoDocumentoResponse response = processoService.anexarDocumento(10L, 20L, 30L);

            assertNotNull(response);
            assertEquals(20L, response.documentoId());
            assertEquals("contrato.pdf", response.nomeOriginal());
            verify(processoDocumentoRepository).save(any(ProcessoDocumento.class));
        }

        @Test
        @DisplayName("Deve listar documentos do processo")
        void deveListarDocumentosDoProcesso() {
            TipoDocumento tipo = new TipoDocumento();
            tipo.setId(1L);
            tipo.setNome("RG");

            Documento doc = new Documento();
            doc.setId(20L);
            doc.setNomeOriginal("doc.pdf");
            doc.setTipoDocumento(tipo);

            Usuario u = new Usuario();
            u.setId(3L);
            u.setNome("User");

            ProcessoDocumento pd = new ProcessoDocumento();
            pd.setId(new ProcessoDocumentoId(10L, 20L));
            pd.setDocumento(doc);
            pd.setAnexadoPorUsuario(u);
            pd.setAnexadoEm(LocalDateTime.now());

            when(processoDocumentoRepository.findByProcessoAppId(10L)).thenReturn(List.of(pd));

            List<ProcessoDocumentoResponse> resultado = processoService.listarDocumentosDoProcesso(10L);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(processoDocumentoRepository).findByProcessoAppId(10L);
        }
    }
}
