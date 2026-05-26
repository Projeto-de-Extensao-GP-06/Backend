package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.entity.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveService — Testes unitários")
class OneDriveServiceTest {

    @Mock private OneDriveTokenAdapter tokenAdapter;
    @Mock private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock private ClienteRepository clienteRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository;

    private OneDriveService service;

    @BeforeEach
    void setup() {
        service = spy(new OneDriveService(
                tokenAdapter, usuarioAutenticacaoAdapter, clienteRepository,
                documentoRepository, usuarioRepository,
                tipoDocumentoRepository, caminhoPadraoDeArmazenamentoRepository
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
            doReturn(respostaEsperada).when(service).uploadDocumento("arquivo.pdf", conteudo, 1L, 1L);

            OneDriveItemResponse resultado = service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L);
            assertEquals("id-1", resultado.id());
        }

        @Test
        @DisplayName("Deve falhar no Graph com upload simples para 4 MB")
        void deveUsarUploadSimplesParaExatamente4MB() {
            byte[] conteudo = new byte[4 * 1024 * 1024];
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            mockCaminho(1L);
            assertThrows(Exception.class, () -> service.uploadDocumento("arquivo.pdf", conteudo, 1L, 1L));
        }

        @Test
        @DisplayName("Deve falhar no Graph com upload grande para > 4 MB")
        void deveUsarUploadGrandeParaArquivoMaior() {
            byte[] conteudo = new byte[5 * 1024 * 1024];
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            mockCaminho(1L);
            assertThrows(Exception.class, () -> service.uploadDocumento("grande.zip", conteudo, 1L, 1L));
        }

        @Test
        @DisplayName("Deve lançar exceção quando caminho não existe")
        void deveLancarExcecaoQuandoCaminhoNaoExiste() {
            when(caminhoPadraoDeArmazenamentoRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(OneDriveErroException.class,
                    () -> service.uploadDocumento("arquivo.pdf", new byte[100], 1L, 99L));
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
        @DisplayName("Deve lançar exceção quando caminho já existe")
        void deveLancarExcecaoQuandoCaminhoJaExiste() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(criarClienteMock()));
            when(caminhoPadraoDeArmazenamentoRepository.existsByCaminho("rafael-silva-12345678901/1234567-89.2024.8.26.0100"))
                    .thenReturn(true);
            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.criarPasta(1L, "1234567-89.2024.8.26.0100"));
            assertTrue(ex.getMessage().contains("Já existe"));
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
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoExcluirPasta() {
            mockCaminho(1L);
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");
            OneDriveErroException ex = assertThrows(OneDriveErroException.class, () -> service.excluirPasta(1L));
            assertTrue(ex.getMessage().contains("Erro ao excluir a pasta"));
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

    private OneDriveItemResponse criarRespostaMock(String id, String nome) {
        return new OneDriveItemResponse(
                id, nome, "arquivo", 1024L, "application/pdf",
                "https://download.url", "https://thumbnail.url",
                OffsetDateTime.now(), OffsetDateTime.now(),
                "/root:/sgpj/usuarios/rafael-silva/1234/" + nome
        );
    }
}
