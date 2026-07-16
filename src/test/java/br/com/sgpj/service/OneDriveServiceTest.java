package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.exception.CaminhoJaExisteException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.models.ItemPreviewInfo;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OneDriveService — Testes unitários")
class OneDriveServiceTest {

    @Mock private OneDriveTokenAdapter tokenAdapter;
    @Mock private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock private ClienteRepository clienteRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository;
    @Mock private ProcessoAppRepository processoAppRepository;
    @Mock private ProcessoDocumentoRepository processoDocumentoRepository;

    private OneDriveService service;

    @BeforeEach
    void setup() {
        service = spy(new OneDriveService(
                tokenAdapter, usuarioAutenticacaoAdapter, clienteRepository,
                documentoRepository, usuarioRepository,
                tipoDocumentoRepository, caminhoPadraoDeArmazenamentoRepository,
                processoAppRepository, processoDocumentoRepository
        ));
    }

    private CaminhoPadraoDeArmazenamento criarCaminhoMock(Long id) {
        CaminhoPadraoDeArmazenamento caminho = new CaminhoPadraoDeArmazenamento();
        caminho.setId(id);
        caminho.setCaminho("rafael-silva-12345678901/1234567-89.2024.8.26.0100");
        return caminho;
    }

    private void mockCaminho(Long id) {
        lenient().when(caminhoPadraoDeArmazenamentoRepository.findById(id))
                .thenReturn(Optional.of(criarCaminhoMock(id)));
    }

    // ========== Upload ==========

    @Nested
    @DisplayName("uploadDocumento — Roteamento por tamanho")
    class UploadDocumentoTest {

        @Test
        @DisplayName("Deve usar upload simples quando arquivo ≤ 4 MB")
        void deveUsarUploadSimplesParaArquivoPequeno() {
            byte[] conteudo = new byte[1024 * 1024]; // 1 MB
            OneDriveItemResponse respostaEsperada = criarRespostaMock("id-1", "arquivo.pdf");
            doReturn(respostaEsperada).when(service).uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, null);

            OneDriveItemResponse resultado = service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, null);
            assertEquals("id-1", resultado.id());
        }

        @Test
        @DisplayName("Deve falhar no Graph com upload simples para 4 MB")
        void deveUsarUploadSimplesParaExatamente4MB() {
            byte[] conteudo = new byte[4 * 1024 * 1024];
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            mockCaminho(1L);
            assertThrows(Exception.class, () -> service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, null));
        }

        @Test
        @DisplayName("Deve falhar no Graph com upload grande para > 4 MB")
        void deveUsarUploadGrandeParaArquivoMaior() {
            byte[] conteudo = new byte[5 * 1024 * 1024];
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            mockCaminho(1L);
            assertThrows(Exception.class, () -> service.uploadDocumento("grande.zip", conteudo, 1L, 1L, null));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class,
                    () -> service.uploadDocumento("arquivo.pdf", new byte[100], 1L, 99L, null));
        }
    }

    // ========== Listar ==========

    @Nested
    @DisplayName("listarArquivos — Listagem de pasta")
    class ListarArquivosTest {

        @Test
        @DisplayName("Deve lançar exceção quando token falha")
        void deveLancarExcecaoQuandoTokenFalha() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenThrow(new OneDriveErroException("Refresh token não encontrado"));
            assertThrows(OneDriveErroException.class, () -> service.listarArquivos(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando drive não encontrado")
        void deveLancarExcecaoQuandoDriveNulo() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            assertThrows(Exception.class, () -> service.listarArquivos(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class, () -> service.listarArquivos(99L));
        }
    }

    // ========== Criar Pasta ==========

    @Nested
    @DisplayName("criarPasta — Criação de pasta")
    class CriarPastaTest {

        private Cliente criarClienteMock() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Rafael Silva");
            cliente.setCpfCnpj("12345678901");
            return cliente;
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoCriarPasta() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(criarClienteMock()));
            when(caminhoPadraoDeArmazenamentoRepository.existsByCaminho(anyString())).thenReturn(false);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.criarPasta(1L, "1234567-89.2024.8.26.0100"));
            assertTrue(ex.getMessage().contains("Erro ao criar a pasta"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ClienteNaoEncontradoException.class,
                    () -> service.criarPasta(99L, "1234567-89.2024.8.26.0100"));
        }

        @Test
        @DisplayName("Deve lançar CaminhoJaExisteException quando caminho já existe")
        void deveLancarExcecaoQuandoCaminhoJaExiste() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(criarClienteMock()));
            when(caminhoPadraoDeArmazenamentoRepository.existsByCaminho("rafael-silva-12345678901/1234567-89.2024.8.26.0100"))
                    .thenReturn(true);
            assertThrows(CaminhoJaExisteException.class,
                    () -> service.criarPasta(1L, "1234567-89.2024.8.26.0100"));
        }
    }

    // ========== Excluir Arquivo ==========

    @Nested
    @DisplayName("excluirArquivo — Exclusão de arquivo")
    class ExcluirArquivoTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoExcluir() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.excluirArquivo(1L, "arquivo.pdf"));
            assertTrue(ex.getMessage().contains("Erro ao excluir o arquivo"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class, () -> service.excluirArquivo(99L, "arquivo.pdf"));
        }
    }

    // ========== Excluir Pasta ==========

    @Nested
    @DisplayName("excluirPasta — Exclusão de pasta")
    class ExcluirPastaTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar após tentativa de exclusão no Graph")
        void deveLancarExcecaoAoExcluirPasta() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            // O delete do Graph é silenciado, mas obterUsuarioLogado() falha pois email não está mockado
            assertThrows(OneDriveErroException.class, () -> service.excluirPasta(1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class, () -> service.excluirPasta(99L));
        }
    }

    // ========== Renomear ==========

    @Nested
    @DisplayName("renomearArquivo — Renomear arquivo")
    class RenomearArquivoTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoRenomear() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.renomearArquivo(1L, "antigo.pdf", "novo.pdf"));
            assertTrue(ex.getMessage().contains("Erro ao renomear o arquivo"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class,
                    () -> service.renomearArquivo(99L, "antigo.pdf", "novo.pdf"));
        }
    }

    // ========== Preview ==========

    @Nested
    @DisplayName("gerarPreviewUrl — Preview de item")
    class GerarPreviewTest {

        @Test
        @DisplayName("Deve lançar exceção quando item não existe")
        void deveLancarExcecaoItemInvalido() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            assertThrows(Exception.class, () -> service.gerarPreviewUrl("item-id-invalido"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(new OneDriveErroException("Refresh token não encontrado"));
            assertThrows(OneDriveErroException.class, () -> service.gerarPreviewUrl("item-id"));
        }
    }

    // ========== gerarPreviewUrl (com mock Graph) ==========

    @Nested
    @DisplayName("gerarPreviewUrl — Com GraphServiceClient mockado")
    class GerarPreviewUrlMockadoTest {

        @Test
        @DisplayName("Deve retornar URL de preview quando Graph responde corretamente")
        void deveRetornarUrlDePreview() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {
                        ItemPreviewInfo previewInfo = mock(ItemPreviewInfo.class);
                        when(previewInfo.getGetUrl()).thenReturn("https://preview.url");
                        lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                                .preview().post(any())).thenReturn(previewInfo);
                    })) {

                String resultado = service.gerarPreviewUrl("item-id");
                assertEquals("https://preview.url", resultado);
            }
        }

        @Test
        @DisplayName("Deve lançar exceção quando URL de preview é nula")
        void deveLancarExcecaoQuandoPreviewUrlNula() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {
                        ItemPreviewInfo previewInfo = mock(ItemPreviewInfo.class);
                        when(previewInfo.getGetUrl()).thenReturn(null);
                        lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                                .preview().post(any())).thenReturn(previewInfo);
                    })) {

                OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                        () -> service.gerarPreviewUrl("item-id"));
                assertTrue(ex.getMessage().contains("Não foi possível gerar preview"));
            }
        }
    }

    // ========== listarArquivos (com mock Graph) ==========

    @Nested
    @DisplayName("listarArquivos — Com GraphServiceClient mockado")
    class ListarArquivosMockadoTest {

        @Test
        @DisplayName("Deve retornar lista de itens quando Graph responde")
        void deveRetornarListaDeItens() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            com.microsoft.graph.models.DriveItem item = new com.microsoft.graph.models.DriveItem();
            item.setName("arquivo.pdf");

            DriveItemCollectionResponse resp = new DriveItemCollectionResponse();
            resp.setValue(List.of(item));

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {
                        com.microsoft.graph.models.Drive drive = new com.microsoft.graph.models.Drive();
                        drive.setId("drive-id");
                        lenient().when(mock.me().drive().get()).thenReturn(drive);
                        lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                                .children().get(any())).thenReturn(resp);
                    })) {

                List<OneDriveItemResponse> resultado = service.listarArquivos(1L);
                assertNotNull(resultado);
                assertFalse(resultado.isEmpty());
                assertEquals("arquivo.pdf", resultado.getFirst().nome());
            }
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando pasta está vazia")
        void deveRetornarListaVaziaQuandoPastaVazia() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            DriveItemCollectionResponse resp = new DriveItemCollectionResponse();
            resp.setValue(Collections.emptyList());

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {
                        com.microsoft.graph.models.Drive drive = new com.microsoft.graph.models.Drive();
                        drive.setId("drive-id");
                        lenient().when(mock.me().drive().get()).thenReturn(drive);
                        lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                                .children().get(any())).thenReturn(resp);
                    })) {

                List<OneDriveItemResponse> resultado = service.listarArquivos(1L);
                assertNotNull(resultado);
                assertTrue(resultado.isEmpty());
            }
        }
    }

    // ========== criarPasta (com mock Graph) ==========

    @Nested
    @DisplayName("criarPasta — Happy path com GraphServiceClient mockado")
    class CriarPastaMockadaTest {

        @Test
        @DisplayName("Deve criar pasta com sucesso e salvar caminho no banco")
        void deveCriarPastaComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Rafael Silva");
            cliente.setCpfCnpj("12345678901");

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(caminhoPadraoDeArmazenamentoRepository.existsByCaminho(
                    "rafael-silva-12345678901/1234567-89.2024.8.26.0100")).thenReturn(false);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {
                        com.microsoft.graph.models.Drive drive = new com.microsoft.graph.models.Drive();
                        drive.setId("drive-id");
                        lenient().when(mock.me().drive().get()).thenReturn(drive);
                    })) {

                OneDriveItemResponse resultado = service.criarPasta(1L, "1234567-89.2024.8.26.0100");
                assertNotNull(resultado);
                assertEquals("rafael-silva-12345678901/1234567-89.2024.8.26.0100", resultado.caminho());
                verify(caminhoPadraoDeArmazenamentoRepository).save(any());
            }
        }
    }

    // ========== excluirArquivo (com mock Graph) ==========

    @Nested
    @DisplayName("excluirArquivo — Com GraphServiceClient mockado")
    class ExcluirArquivoMockadoTest {

        @Test
        @DisplayName("Deve excluir arquivo e inativar documento no banco")
        void deveExcluirArquivoEInativarDocumento() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            Documento doc = new Documento();
            doc.setId(1L);
            doc.setAtivo(true);
            when(documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue("arquivo.pdf", 1L))
                    .thenReturn(Optional.of(doc));

            Usuario usuario = new Usuario();
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("user@test.com");
            when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {})) {

                assertDoesNotThrow(() -> service.excluirArquivo(1L, "arquivo.pdf"));
                verify(documentoRepository).save(doc);
                assertFalse(doc.getAtivo());
                assertNotNull(doc.getInativadoEm());
            }
        }

        @Test
        @DisplayName("Deve excluir arquivo sem atualizar banco quando documento não encontrado")
        void deveExcluirArquivoSemDocumentoNoBanco() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            when(documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue("arquivo.pdf", 1L))
                    .thenReturn(Optional.empty());

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {})) {

                assertDoesNotThrow(() -> service.excluirArquivo(1L, "arquivo.pdf"));
                verify(documentoRepository, never()).save(any());
            }
        }
    }

    // ========== excluirPasta (com mock Graph) ==========

    @Nested
    @DisplayName("excluirPasta — Com GraphServiceClient mockado")
    class ExcluirPastaMockadaTest {

        private Usuario criarUsuarioMock() {
            Usuario usuario = new Usuario();
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("user@test.com");
            when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            return usuario;
        }

        @Test
        @DisplayName("Deve excluir pasta, inativar documentos ativos e remover caminho do banco")
        void deveExcluirPastaComDocumentosAtivos() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            criarUsuarioMock();

            Documento doc = new Documento();
            doc.setId(1L);
            doc.setAtivo(true);
            when(documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(1L)).thenReturn(List.of(doc));
            when(documentoRepository.findByPadraoArmazenamentoId(1L)).thenReturn(List.of(doc));

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {})) {

                assertDoesNotThrow(() -> service.excluirPasta(1L));
                verify(caminhoPadraoDeArmazenamentoRepository).delete(any());
                verify(documentoRepository, times(2)).saveAll(any());
                assertFalse(doc.getAtivo());
                assertNotNull(doc.getInativadoEm());
            }
        }

        @Test
        @DisplayName("Deve excluir pasta sem documentos sem chamar saveAll")
        void deveExcluirPastaSemDocumentos() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            criarUsuarioMock();

            when(documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(1L)).thenReturn(Collections.emptyList());
            when(documentoRepository.findByPadraoArmazenamentoId(1L)).thenReturn(Collections.emptyList());

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {})) {

                assertDoesNotThrow(() -> service.excluirPasta(1L));
                verify(caminhoPadraoDeArmazenamentoRepository).delete(any());
                verify(documentoRepository, never()).saveAll(any());
            }
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando usuário logado não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("user@test.com");
            when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    (mock, ctx) -> {})) {

                OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                        () -> service.excluirPasta(1L));
                assertTrue(ex.getMessage().contains("Usuário logado não encontrado"));
            }
        }
    }

    // ========== renomearArquivo (com mock Graph) ==========

    @Nested
    @DisplayName("renomearArquivo — Com GraphServiceClient mockado")
    class RenomearArquivoMockadoTest {

        private MockedConstruction.MockInitializer<GraphServiceClient> previewStub() {
            return (mock, ctx) -> {
                ItemPreviewInfo previewInfo = mock(ItemPreviewInfo.class);
                when(previewInfo.getGetUrl()).thenReturn("https://preview.url");
                lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                        .preview().post(any())).thenReturn(previewInfo);
            };
        }

        @Test
        @DisplayName("Deve renomear arquivo e atualizar documento no banco")
        void deveRenomearArquivoEAtualizarDocumento() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            Documento doc = new Documento();
            doc.setId(1L);
            when(documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue("antigo.pdf", 1L))
                    .thenReturn(Optional.of(doc));

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveItemResponse resultado = service.renomearArquivo(1L, "antigo.pdf", "novo.pdf");
                assertNotNull(resultado);
                verify(documentoRepository).save(doc);
                assertEquals("novo.pdf", doc.getNomeOriginal());
            }
        }

        @Test
        @DisplayName("Deve renomear arquivo sem atualizar banco quando documento não encontrado")
        void deveRenomearArquivoSemDocumentoNoBanco() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            when(documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue("antigo.pdf", 1L))
                    .thenReturn(Optional.empty());

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveItemResponse resultado = service.renomearArquivo(1L, "antigo.pdf", "novo.pdf");
                assertNotNull(resultado);
                verify(documentoRepository, never()).save(any());
            }
        }
    }

    // ========== uploadDocumento / registrarDocumento (com mock Graph) ==========

    @Nested
    @DisplayName("uploadDocumento — registrarDocumento com GraphServiceClient mockado")
    class RegistrarDocumentoMockadoTest {

        private void mockDependenciasRegistro() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            Usuario usuario = new Usuario();
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("user@test.com");
            when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(tipoDocumentoRepository.findById(1L)).thenReturn(Optional.of(new TipoDocumento()));
            when(documentoRepository.findByOnedriveItemId(any())).thenReturn(Optional.empty());
        }

        private MockedConstruction.MockInitializer<GraphServiceClient> previewStub() {
            return (mock, ctx) -> {
                ItemPreviewInfo previewInfo = mock(ItemPreviewInfo.class);
                when(previewInfo.getGetUrl()).thenReturn("https://preview.url");
                lenient().when(mock.drives().byDriveId(any()).items().byDriveItemId(any())
                        .preview().post(any())).thenReturn(previewInfo);
            };
        }

        @Test
        @DisplayName("Deve registrar novo documento após upload simples")
        void deveRegistrarNovoDocumentoAposUploadSimples() {
            mockCaminho(1L);
            mockDependenciasRegistro();

            Documento documentoSalvo = new Documento();
            documentoSalvo.setId(1L);
            when(documentoRepository.save(any())).thenReturn(documentoSalvo);

            byte[] conteudo = new byte[1024]; // 1 KB — upload simples

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveItemResponse resultado = service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, null);
                assertNotNull(resultado);
                verify(documentoRepository).save(any());
                verify(processoDocumentoRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Deve atualizar documento existente ao fazer upload do mesmo item")
        void deveAtualizarDocumentoExistente() {
            mockCaminho(1L);
            mockDependenciasRegistro();

            Documento existente = new Documento();
            existente.setId(99L); // já tem ID → não é novo
            when(documentoRepository.findByOnedriveItemId(any())).thenReturn(Optional.of(existente));
            when(documentoRepository.save(any())).thenReturn(existente);

            byte[] conteudo = new byte[1024];

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveItemResponse resultado = service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, null);
                assertNotNull(resultado);
                // dataHoraCriacao não deve ser alterada para documento existente
                assertNull(existente.getDataHoraCriacao());
            }
        }

        @Test
        @DisplayName("Deve vincular documento ao processo quando processoAppId é fornecido")
        void deveVincularDocumentoAoProcesso() {
            mockCaminho(1L);
            mockDependenciasRegistro();

            Documento documentoSalvo = new Documento();
            documentoSalvo.setId(1L);
            when(documentoRepository.save(any())).thenReturn(documentoSalvo);

            ProcessoApp processo = new ProcessoApp();
            processo.setId(10L);
            when(processoAppRepository.findById(10L)).thenReturn(Optional.of(processo));

            byte[] conteudo = new byte[1024];

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveItemResponse resultado = service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, 10L);
                assertNotNull(resultado);
                verify(processoDocumentoRepository).save(any());
            }
        }

        @Test
        @DisplayName("Deve lançar ObjetoNaoEncontradoException quando processo não encontrado ao vincular")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            mockCaminho(1L);
            mockDependenciasRegistro();

            Documento documentoSalvo = new Documento();
            documentoSalvo.setId(1L);
            when(documentoRepository.save(any())).thenReturn(documentoSalvo);
            when(processoAppRepository.findById(99L)).thenReturn(Optional.empty());

            byte[] conteudo = new byte[1024];

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                assertThrows(ObjetoNaoEncontradoException.class,
                        () -> service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L, 99L));
            }
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando tipoDocumento não encontrado")
        void deveLancarExcecaoQuandoTipoDocumentoNaoEncontrado() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            when(tipoDocumentoRepository.findById(99L)).thenReturn(Optional.empty());

            Usuario usuario = new Usuario();
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("user@test.com");
            when(usuarioRepository.findByEmail("user@test.com")).thenReturn(Optional.of(usuario));
            when(documentoRepository.findByOnedriveItemId(any())).thenReturn(Optional.empty());

            byte[] conteudo = new byte[1024];

            try (MockedConstruction<GraphServiceClient> ignored = mockConstruction(
                    GraphServiceClient.class,
                    withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                    previewStub())) {

                OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                        () -> service.uploadDocumento("arquivo.pdf", conteudo, 99L, 1L, null));
                assertTrue(ex.getMessage().contains("Tipo de documento não encontrado"));
            }
        }
    }

    private OneDriveItemResponse criarRespostaMock(String id, String nome) {
        return new OneDriveItemResponse(
                id, nome, "arquivo", 1024L, "application/pdf",
                "https://download.url", "https://thumbnail.url",
                OffsetDateTime.now(), OffsetDateTime.now(),
                "/root:/sgpj/usuarios/rafael-silva/1234/" + nome
        );
    }
}
