package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.onedrive.OneDriveDocumentManager;
import br.com.sgpj.service.useCases.onedrive.OneDriveGraphGateway;
import br.com.sgpj.service.useCases.onedrive.OneDrivePathHelper;
import com.microsoft.graph.models.DriveItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OneDriveService constrói internamente o OneDriveGraphGateway e o OneDriveDocumentManager
 * (não são injetados via Spring). Para isolar o teste da API do Microsoft Graph,
 * substituímos esses campos por mocks via reflexão após a construção do serviço.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OneDriveService")
class OneDriveServiceTest {

    @Mock private OneDriveTokenAdapter tokenAdapter;
    @Mock private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock private ClienteRepository clienteRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @Mock private ProcessoAppRepository processoAppRepository;
    @Mock private ProcessoDocumentoRepository processoDocumentoRepository;
    @Mock private RegistroHistoricoProcesso registroHistorico;

    @Mock private OneDriveGraphGateway graphGateway;
    @Mock private OneDriveDocumentManager documentManager;

    private OneDriveService oneDriveService;

    @BeforeEach
    void setUp() {
        oneDriveService = new OneDriveService(
                tokenAdapter,
                usuarioAutenticacaoAdapter,
                clienteRepository,
                documentoRepository,
                usuarioRepository,
                tipoDocumentoRepository,
                caminhoRepository,
                processoAppRepository,
                processoDocumentoRepository,
                registroHistorico
        );

        ReflectionTestUtils.setField(oneDriveService, "graphGateway", graphGateway);
        ReflectionTestUtils.setField(oneDriveService, "documentManager", documentManager);
    }

    private CaminhoPadraoDeArmazenamento caminhoComId(Long id, String caminho) {
        CaminhoPadraoDeArmazenamento c = new CaminhoPadraoDeArmazenamento();
        c.setId(id);
        c.setCaminho(caminho);
        return c;
    }

    @Nested
    @DisplayName("listarArquivos")
    class ListarArquivosTest {

        @Test
        @DisplayName("Deve resolver o caminho e delegar a listagem ao gateway")
        void deveListarArquivosDoCaminho() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x-123");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));

            List<OneDriveItemResponse> esperado = List.of(mock(OneDriveItemResponse.class));
            when(graphGateway.listarArquivos(OneDrivePathHelper.caminhoDaPasta("empresa-x-123")))
                    .thenReturn(esperado);

            List<OneDriveItemResponse> resultado = oneDriveService.listarArquivos(1L);

            assertEquals(esperado, resultado);
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando o caminho não existir")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(OneDriveErroException.class, () -> oneDriveService.listarArquivos(99L));
            verifyNoInteractions(graphGateway);
        }
    }

    @Nested
    @DisplayName("criarPasta")
    class CriarPastaTest {

        @Test
        @DisplayName("Deve lançar ClienteNaoEncontradoException quando o cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ClienteNaoEncontradoException.class,
                    () -> oneDriveService.criarPasta(1L));
            verifyNoInteractions(graphGateway);
        }

        @Test
        @DisplayName("Deve reutilizar o caminho único já existente para o cliente")
        void deveReutilizarCaminhoDoCliente() {
            Cliente cliente = new Cliente();
            cliente.setNomeRazaoSocial("Empresa X");
            cliente.setCpfCnpj("12345678000199");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(caminhoRepository.findByClienteId(1L)).thenReturn(List.of(
                    caminhoComId(2L, OneDrivePathHelper.gerarNomePasta(cliente))));
            DriveItem pasta = new DriveItem();
            pasta.setId("pasta-1");
            when(graphGateway.garantirPasta(anyString(), anyString())).thenReturn(pasta);

            OneDriveItemResponse resultado = oneDriveService.criarPasta(1L);

            assertEquals("pasta-1", resultado.id());
            verify(caminhoRepository, never()).save(any(CaminhoPadraoDeArmazenamento.class));
        }

        @Test
        @DisplayName("Deve criar a pasta no OneDrive e persistir o novo caminho")
        void deveCriarPastaComSucesso() {
            Cliente cliente = new Cliente();
            cliente.setNomeRazaoSocial("Empresa X");
            cliente.setCpfCnpj("12345678000199");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(caminhoRepository.findByClienteId(1L)).thenReturn(List.of());

            DriveItem pastaCriada = new DriveItem();
            pastaCriada.setId("item-123");
            when(graphGateway.garantirPasta(anyString(), anyString())).thenReturn(pastaCriada);

            OneDriveItemResponse resultado = oneDriveService.criarPasta(1L);

            assertNotNull(resultado);
            assertEquals("item-123", resultado.id());
            verify(graphGateway).garantirPasta(eq(OneDrivePathHelper.caminhoRaiz() + ":"), anyString());
            verify(caminhoRepository).save(any(CaminhoPadraoDeArmazenamento.class));
        }

        @Test
        @DisplayName("Deve envolver falhas inesperadas do gateway em OneDriveErroException")
        void deveEnvolverErroInesperadoDoGateway() {
            Cliente cliente = new Cliente();
            cliente.setNomeRazaoSocial("Empresa X");
            cliente.setCpfCnpj("12345678000199");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(caminhoRepository.findByClienteId(1L)).thenReturn(List.of());
            when(graphGateway.garantirPasta(anyString(), anyString()))
                    .thenThrow(new RuntimeException("falha de rede"));

            assertThrows(OneDriveErroException.class,
                    () -> oneDriveService.criarPasta(1L));
            verify(caminhoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("uploadDocumento")
    class UploadDocumentoTest {

        @Test
        @DisplayName("Deve usar upload simples para arquivos dentro do limite")
        void deveUsarUploadSimplesParaArquivoPequeno() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x-123");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));

            byte[] conteudo = new byte[100];
            DriveItem item = new DriveItem();
            item.setId("arquivo-1");
            when(graphGateway.uploadSimples(anyString(), eq(conteudo))).thenReturn(item);
            when(graphGateway.gerarPreviewUrl("arquivo-1")).thenReturn("http://preview");

            OneDriveItemResponse resultado = oneDriveService.uploadDocumento(
                    "documento.pdf", conteudo, 2L, 1L, 3L);

            assertEquals("arquivo-1", resultado.id());
            verify(graphGateway).uploadSimples(anyString(), eq(conteudo));
            verify(graphGateway, never()).uploadGrande(anyString(), any());
            verify(documentManager).registrarDocumento(resultado, 2L, caminho, 3L);
        }

        @Test
        @DisplayName("Deve usar upload grande para arquivos acima do limite")
        void deveUsarUploadGrandeParaArquivoGrande() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x-123");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));

            byte[] conteudo = new byte[(int) OneDrivePathHelper.LIMITE_UPLOAD_SIMPLES + 1];
            DriveItem item = new DriveItem();
            item.setId("arquivo-2");
            when(graphGateway.uploadGrande(anyString(), eq(conteudo))).thenReturn(item);
            when(graphGateway.gerarPreviewUrl("arquivo-2")).thenReturn("http://preview");

            oneDriveService.uploadDocumento("documento-grande.pdf", conteudo, 2L, 1L, 3L);

            verify(graphGateway).uploadGrande(anyString(), eq(conteudo));
            verify(graphGateway, never()).uploadSimples(anyString(), any());
        }
    }

    @Nested
    @DisplayName("excluirArquivo")
    class ExcluirArquivoTest {

        @Test
        @DisplayName("Deve excluir no gateway e inativar o documento no banco")
        void deveExcluirArquivoEInativarDocumento() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x-123");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));

            oneDriveService.excluirArquivo(1L, "documento.pdf");

            verify(graphGateway).excluir(anyString());
            verify(documentManager).inativarDocumentoPorArquivo(caminho, "documento.pdf");
        }
    }

    @Nested
    @DisplayName("excluirPasta")
    class ExcluirPastaTest {

        @Test
        @DisplayName("Deve excluir a pasta, limpar o banco e remover a pasta pai se ficar vazia")
        void deveExcluirPastaEPastaPaiVazia() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x/0001234-56.2024.8.26.0100");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));
            when(graphGateway.listarFilhos(anyString())).thenReturn(List.of());

            oneDriveService.excluirPasta(1L);

            verify(documentManager).inativarDocumentosDaPasta(caminho);
            verify(documentManager).desvincularDocumentosDaPasta(caminho);
            verify(documentManager).removerCaminho(caminho);
            verify(graphGateway, times(2)).excluir(anyString()); // pasta do processo + pasta pai vazia
        }

        @Test
        @DisplayName("Deve seguir limpando o banco mesmo se a pasta não existir mais no OneDrive")
        void deveContinuarLimpezaQuandoPastaNaoExisteNoOneDrive() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x/0001234-56.2024.8.26.0100");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));
            doThrow(new OneDriveErroException("não encontrado")).when(graphGateway).excluir(anyString());

            oneDriveService.excluirPasta(1L);

            verify(documentManager).inativarDocumentosDaPasta(caminho);
            verify(documentManager).removerCaminho(caminho);
        }

        @Test
        @DisplayName("Não deve remover a pasta pai se ela ainda tiver arquivos")
        void naoDeveRemoverPastaPaiComArquivos() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x/0001234-56.2024.8.26.0100");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));
            when(graphGateway.listarFilhos(anyString())).thenReturn(List.of(new DriveItem()));

            oneDriveService.excluirPasta(1L);

            // uma chamada de excluir para a própria pasta; nenhuma para a pasta pai
            verify(graphGateway, times(1)).excluir(anyString());
        }
    }

    @Nested
    @DisplayName("renomearArquivo")
    class RenomearArquivoTest {

        @Test
        @DisplayName("Deve renomear no gateway e atualizar o registro do documento")
        void deveRenomearArquivoComSucesso() {
            CaminhoPadraoDeArmazenamento caminho = caminhoComId(1L, "empresa-x-123");
            when(caminhoRepository.findById(1L)).thenReturn(Optional.of(caminho));

            DriveItem atualizado = new DriveItem();
            atualizado.setId("arquivo-1");
            atualizado.setName("novo-nome.pdf");
            when(graphGateway.renomearArquivo(anyString(), eq("novo-nome.pdf"))).thenReturn(atualizado);
            when(graphGateway.gerarPreviewUrl("arquivo-1")).thenReturn("http://preview");

            OneDriveItemResponse resultado = oneDriveService.renomearArquivo(1L, "antigo-nome.pdf", "novo-nome.pdf");

            assertEquals("arquivo-1", resultado.id());
            verify(documentManager).atualizarDocumentoRenomeado(caminho, "antigo-nome.pdf", resultado);
        }
    }

    @Nested
    @DisplayName("gerarPreviewUrl")
    class GerarPreviewUrlTest {

        @Test
        @DisplayName("Deve delegar diretamente ao gateway")
        void deveDelegarAoGateway() {
            when(graphGateway.gerarPreviewUrl("item-1")).thenReturn("http://preview");

            String resultado = oneDriveService.gerarPreviewUrl("item-1");

            assertEquals("http://preview", resultado);
        }
    }
}
