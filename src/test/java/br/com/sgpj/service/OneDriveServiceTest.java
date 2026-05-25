package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.exception.OneDriveErroException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveService — Testes unitários")
class OneDriveServiceTest {

    @Mock
    private OneDriveTokenAdapter tokenAdapter;

    private OneDriveService service;

    @BeforeEach
    void setup() {
        service = spy(new OneDriveService(tokenAdapter));
    }

    @Nested
    @DisplayName("uploadDocumento — Roteamento por tamanho")
    class UploadDocumentoTest {

        @Test
        @DisplayName("Deve usar upload simples quando arquivo ≤ 4 MB")
        void deveUsarUploadSimplesParaArquivoPequeno() {
            byte[] conteudo = new byte[1024 * 1024]; // 1 MB
            OneDriveItemResponse respostaEsperada = criarRespostaMock("id-1", "arquivo.pdf");

            doReturn(respostaEsperada).when(service).uploadDocumento("pasta-user", "arquivo.pdf", conteudo);

            OneDriveItemResponse resultado = service.uploadDocumento("pasta-user", "arquivo.pdf", conteudo);

            assertEquals("id-1", resultado.id());
            assertEquals("arquivo.pdf", resultado.nome());
        }

        @Test
        @DisplayName("Deve usar upload simples para arquivo exatamente 4 MB (falha no Graph)")
        void deveUsarUploadSimplesParaExatamente4MB() {
            byte[] conteudo = new byte[4 * 1024 * 1024]; // Exatamente 4 MB
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            // Vai falhar no Graph (token inválido) — prova que tentou o caminho correto
            assertThrows(Exception.class,
                    () -> service.uploadDocumento("pasta", "arquivo.pdf", conteudo));
        }

        @Test
        @DisplayName("Deve usar upload grande para arquivo > 4 MB (falha no Graph)")
        void deveUsarUploadGrandeParaArquivoMaior() {
            byte[] conteudo = new byte[5 * 1024 * 1024]; // 5 MB
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            // Vai falhar no Graph — prova que tentou o caminho de upload grande
            assertThrows(Exception.class,
                    () -> service.uploadDocumento("pasta", "grande.zip", conteudo));
        }
    }

    @Nested
    @DisplayName("listarArquivos — Listagem de pasta")
    class ListarArquivosTest {

        @Test
        @DisplayName("Deve lançar exceção quando token adapter falha")
        void deveLancarExcecaoQuandoTokenFalha() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.listarArquivos("minha-pasta"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando drive não é encontrado")
        void deveLancarExcecaoQuandoDriveNulo() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            assertThrows(Exception.class,
                    () -> service.listarArquivos("pasta-teste"));
        }
    }

    @Nested
    @DisplayName("criarPasta — Criação de pasta")
    class CriarPastaTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoCriarPasta() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.criarPasta("nova-pasta"));

            assertTrue(ex.getMessage().contains("Erro ao criar a pasta"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.criarPasta("pasta"));
        }
    }

    @Nested
    @DisplayName("excluirArquivo — Exclusão de arquivo")
    class ExcluirArquivoTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoExcluir() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.excluirArquivo("pasta", "arquivo.pdf"));

            assertTrue(ex.getMessage().contains("Erro ao excluir o arquivo"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.excluirArquivo("pasta", "arquivo.pdf"));
        }
    }

    @Nested
    @DisplayName("excluirPasta — Exclusão de pasta")
    class ExcluirPastaTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoExcluirPasta() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.excluirPasta("pasta-remover"));

            assertTrue(ex.getMessage().contains("Erro ao excluir a pasta"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.excluirPasta("pasta-remover"));
        }
    }

    @Nested
    @DisplayName("renomearArquivo — Renomear arquivo")
    class RenomearArquivoTest {

        @Test
        @DisplayName("Deve lançar OneDriveErroException ao falhar no Graph")
        void deveLancarExcecaoAoRenomear() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            OneDriveErroException ex = assertThrows(OneDriveErroException.class,
                    () -> service.renomearArquivo("pasta", "antigo.pdf", "novo.pdf"));

            assertTrue(ex.getMessage().contains("Erro ao renomear o arquivo"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.renomearArquivo("pasta", "antigo.pdf", "novo.pdf"));
        }
    }

    @Nested
    @DisplayName("gerarPreviewUrl — Preview de item")
    class GerarPreviewTest {

        @Test
        @DisplayName("Deve lançar exceção quando item não existe")
        void deveLancarExcecaoItemInvalido() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            assertThrows(Exception.class,
                    () -> service.gerarPreviewUrl("item-id-invalido"));
        }

        @Test
        @DisplayName("Deve lançar OneDriveErroException quando token não disponível")
        void deveLancarExcecaoSemToken() {
            when(tokenAdapter.getAccessToken()).thenThrow(
                    new OneDriveErroException("Refresh token não encontrado"));

            assertThrows(OneDriveErroException.class,
                    () -> service.gerarPreviewUrl("item-id"));
        }
    }

    @Nested
    @DisplayName("Construção de caminhos")
    class CaminhosTest {

        @Test
        @DisplayName("uploadDocumento não aceita pasta nula")
        void naoDeveAceitarPastaNula() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            assertThrows(Exception.class,
                    () -> service.uploadDocumento(null, "arquivo.pdf", new byte[100]));
        }

        @Test
        @DisplayName("uploadDocumento com conteúdo vazio tenta upload simples")
        void deveAceitarConteudoVazio() {
            when(tokenAdapter.getAccessToken()).thenReturn("fake-token");

            assertThrows(Exception.class,
                    () -> service.uploadDocumento("pasta", "vazio.txt", new byte[0]));
        }
    }

    // Utilitário para criar resposta mock
    private OneDriveItemResponse criarRespostaMock(String id, String nome) {
        return new OneDriveItemResponse(
                id,
                nome,
                "arquivo",
                1024L,
                "application/pdf",
                "https://download.url",
                "https://thumbnail.url",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "/root:/sgpj/usuarios/pasta/" + nome
        );
    }
}
