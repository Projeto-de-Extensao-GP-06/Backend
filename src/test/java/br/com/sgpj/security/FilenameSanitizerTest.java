package br.com.sgpj.security;

import br.com.sgpj.exception.OneDriveErroException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FilenameSanitizer - Testes de sanitização de nomes de arquivos")
class FilenameSanitizerTest {

    @Nested
    @DisplayName("sanitize - Testes de sanitização com exceções")
    class SanitizeTest {

        @Test
        @DisplayName("Deve sanitizar nome de arquivo válido")
        void deveSanitizarNomeValido() {
            String resultado = FilenameSanitizer.sanitize("documento.pdf");
            assertEquals("documento.pdf", resultado);
        }

        @Test
        @DisplayName("Deve remover caracteres perigosos do Windows")
        void deveRemoverCaracteresPerigososWindows() {
            String resultado = FilenameSanitizer.sanitize("arquivo<teste>.txt");
            assertEquals("arquivo_teste_.txt", resultado);
        }

        @Test
        @DisplayName("Deve lançar exceção para caracteres de controle")
        void deveRemoverCaracteresControle() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("arquivo\u0000teste.txt"));
        }

        @Test
        @DisplayName("Deve lançar exceção para null")
        void deveLancarExcecaoParaNull() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize(null));
        }

        @Test
        @DisplayName("Deve lançar exceção para nome vazio")
        void deveLancarExcecaoParaNomeVazio() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize(""));
        }

        @Test
        @DisplayName("Deve lançar exceção para nome em branco")
        void deveLancarExcecaoParaNomeEmBranco() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("   "));
        }

        @Test
        @DisplayName("Deve lançar exceção para path traversal ../")
        void deveLancarExcecaoParaPathTraversal() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("../../../etc/passwd"));
        }

        @Test
        @DisplayName("Deve lançar exceção para path traversal ..\\")
        void deveLancarExcecaoParaPathTraversalWindows() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("..\\..\\windows\\system32"));
        }

        @Test
        @DisplayName("Deve lançar exceção para separadores de caminho")
        void deveLancarExcecaoParaSeparadoresCaminho() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("pasta/arquivo.txt"));
        }

        @Test
        @DisplayName("Deve lançar exceção para nome que começa com ponto")
        void deveLancarExcecaoParaNomeComecaComPonto() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize(".hidden"));
        }

        @Test
        @DisplayName("Deve remover espaços e pontos do início e fim")
        void deveRemoverEspacosPontosInicioFim() {
            String resultado = FilenameSanitizer.sanitize("  .arquivo.  ");
            assertEquals("arquivo", resultado);
        }

        @Test
        @DisplayName("Deve truncar nome muito longo")
        void deveTruncarNomeMuitoLongo() {
            String nomeLongo = "a".repeat(300);
            String resultado = FilenameSanitizer.sanitize(nomeLongo);
            assertEquals(255, resultado.length());
        }

        @Test
        @DisplayName("Deve lançar exceção se sanitização resultar em nome vazio")
        void deveLancarExcecaoSeSanitizacaoResultarVazio() {
            assertThrows(OneDriveErroException.class, () -> FilenameSanitizer.sanitize("..."));
        }
    }

    @Nested
    @DisplayName("isValid - Testes de validação")
    class IsValidTest {

        @Test
        @DisplayName("Deve retornar true para nome válido")
        void deveRetornarTrueParaNomeValido() {
            assertTrue(FilenameSanitizer.isValid("documento.pdf"));
        }

        @Test
        @DisplayName("Deve retornar false para null")
        void deveRetornarFalseParaNull() {
            assertFalse(FilenameSanitizer.isValid(null));
        }

        @Test
        @DisplayName("Deve retornar false para nome vazio")
        void deveRetornarFalseParaNomeVazio() {
            assertFalse(FilenameSanitizer.isValid(""));
        }

        @Test
        @DisplayName("Deve retornar false para path traversal")
        void deveRetornarFalseParaPathTraversal() {
            assertFalse(FilenameSanitizer.isValid("../../../etc/passwd"));
        }

        @Test
        @DisplayName("Deve retornar false para nome muito longo")
        void deveRetornarFalseParaNomeMuitoLongo() {
            assertFalse(FilenameSanitizer.isValid("a".repeat(300)));
        }
    }

    @Nested
    @DisplayName("sanitizeSafe - Testes de sanitização segura sem exceções")
    class SanitizeSafeTest {

        @Test
        @DisplayName("Deve sanitizar nome válido normalmente")
        void deveSanitizarNomeValidoNormalmente() {
            String resultado = FilenameSanitizer.sanitizeSafe("documento.pdf");
            assertEquals("documento.pdf", resultado);
        }

        @Test
        @DisplayName("Deve retornar default para null")
        void deveRetornarDefaultParaNull() {
            String resultado = FilenameSanitizer.sanitizeSafe(null);
            assertEquals("arquivo_sem_nome", resultado);
        }

        @Test
        @DisplayName("Deve retornar default para nome vazio")
        void deveRetornarDefaultParaNomeVazio() {
            String resultado = FilenameSanitizer.sanitizeSafe("");
            assertEquals("arquivo_sem_nome", resultado);
        }

        @Test
        @DisplayName("Deve retornar default para nome em branco")
        void deveRetornarDefaultParaNomeEmBranco() {
            String resultado = FilenameSanitizer.sanitizeSafe("   ");
            assertEquals("arquivo_sem_nome", resultado);
        }

        @Test
        @DisplayName("Deve sanitizar mesmo com path traversal sem lançar exceção")
        void deveSanitizarPathTraversalSemExcecao() {
            String resultado = FilenameSanitizer.sanitizeSafe("../../../etc/passwd");
            assertNotNull(resultado);
            assertNotEquals("../../../etc/passwd", resultado);
        }

        @Test
        @DisplayName("Deve retornar default se sanitização falhar completamente")
        void deveRetornarDefaultSeFalharCompletamente() {
            String resultado = FilenameSanitizer.sanitizeSafe("...");
            assertEquals("arquivo_sanitizado", resultado);
        }

        @Test
        @DisplayName("Deve remover caracteres perigosos sem lançar exceção")
        void deveRemoverCaracteresPerigososSemExcecao() {
            String resultado = FilenameSanitizer.sanitizeSafe("arquivo<teste>.txt");
            assertEquals("arquivo_teste_.txt", resultado);
        }
    }

    @Nested
    @DisplayName("Casos específicos de segurança")
    class SecurityTest {

        @Test
        @DisplayName("Deve prevenir null byte injection")
        void devePrevenirNullByteInjection() {
            String resultado = FilenameSanitizer.sanitizeSafe("arquivo\u0000.pdf");
            assertFalse(resultado.contains("\u0000"));
        }

        @Test
        @DisplayName("Deve prevenir caracteres de controle")
        void devePrevenirCaracteresControle() {
            String resultado = FilenameSanitizer.sanitizeSafe("arquivo\u001B.pdf");
            assertFalse(resultado.contains("\u001B"));
        }

        @Test
        @DisplayName("Deve remover pipes e outros caracteres Windows")
        void deveRemoverCaracteresWindows() {
            String resultado = FilenameSanitizer.sanitizeSafe("arquivo|teste:questão*arquivo.pdf");
            assertEquals("arquivo_teste_questão_arquivo.pdf", resultado);
        }

        @Test
        @DisplayName("Deve lidar com nomes com espaços")
        void deveLidarComNomesComEspacos() {
            String resultado = FilenameSanitizer.sanitizeSafe("meu arquivo documento.pdf");
            assertEquals("meu arquivo documento.pdf", resultado);
        }

        @Test
        @DisplayName("Deve lidar com nomes unicode")
        void deveLidarComNomesUnicode() {
            String resultado = FilenameSanitizer.sanitizeSafe("documento_café.pdf");
            assertEquals("documento_café.pdf", resultado);
        }
    }
}