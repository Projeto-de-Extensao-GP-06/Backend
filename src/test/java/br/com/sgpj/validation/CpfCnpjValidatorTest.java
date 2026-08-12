package br.com.sgpj.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para {@link CpfCnpjValidator}.
 *
 * <p>Todos os valores de CPF e CNPJ foram verificados matematicamente
 * pelo algoritmo Mod11 antes de serem incluídos como casos de teste.
 */
@DisplayName("CpfCnpjValidator")
class CpfCnpjValidatorTest {

    private CpfCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfCnpjValidator();
    }

    // =========================================================================
    // Nulos e brancos — delegados para @NotBlank / @NotNull
    // =========================================================================

    @Nested
    @DisplayName("Valores nulos e em branco")
    class NulosEBrancos {

        @Test
        @DisplayName("null deve retornar true")
        void nuloDeveRetornarTrue() {
            assertTrue(validator.isValid(null, null));
        }

        @Test
        @DisplayName("string vazia deve retornar true")
        void vazioDeveRetornarTrue() {
            assertTrue(validator.isValid("", null));
        }

        @Test
        @DisplayName("string em branco deve retornar true")
        void brancoDeveRetornarTrue() {
            assertTrue(validator.isValid("   ", null));
        }
    }

    // =========================================================================
    // CPF válidos
    // =========================================================================

    @Nested
    @DisplayName("CPF válido")
    class CpfValido {

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "12345678909",      // sem máscara  — d1=0, d2=9
                "52998224725",      // sem máscara  — d1=2, d2=5
        })
        @DisplayName("CPF sem máscara deve ser aceito")
        void cpfSemMascaraValido(String cpf) {
            assertTrue(validator.isValid(cpf, null));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "123.456.789-09",   // com máscara  — d1=0, d2=9
                "529.982.247-25",   // com máscara  — d1=2, d2=5
        })
        @DisplayName("CPF com máscara deve ser aceito")
        void cpfComMascaraValido(String cpf) {
            assertTrue(validator.isValid(cpf, null));
        }
    }

    // =========================================================================
    // CPF inválidos
    // =========================================================================

    @Nested
    @DisplayName("CPF inválido")
    class CpfInvalido {

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "11111111111",      // todos os dígitos iguais
                "00000000000",      // todos zeros
                "22222222222",      // todos iguais
        })
        @DisplayName("CPF com dígitos uniformes deve ser rejeitado")
        void cpfDigitosUniformes(String cpf) {
            assertFalse(validator.isValid(cpf, null));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "12345678900",      // 2º dígito errado (esperado 9, informado 0)
                "12345678908",      // 2º dígito errado (esperado 9, informado 8)
                "52998224720",      // 2º dígito errado (esperado 5, informado 0)
        })
        @DisplayName("CPF com dígito verificador inválido deve ser rejeitado")
        void cpfDigitoVerificadorInvalido(String cpf) {
            assertFalse(validator.isValid(cpf, null));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "12345678919",      // 1º dígito errado (esperado 0, informado 1) — cobre linha 62
                "52998224735",      // 1º dígito errado (esperado 2, informado 3) — cobre linha 62
        })
        @DisplayName("CPF com 1º dígito verificador inválido deve ser rejeitado sem avaliar o 2º")
        void cpfPrimeiroDigitoVerificadorInvalido(String cpf) {
            // Estes CPFs falham especificamente no branch da linha 62 do CpfCnpjValidator
            // (return false após comparar o 1º dígito calculado com o informado),
            // garantindo cobertura do caminho que interrompe a execução antes do 2º dígito.
            assertFalse(validator.isValid(cpf, null));
        }
    }

    // =========================================================================
    // CNPJ numérico válidos
    // =========================================================================

    @Nested
    @DisplayName("CNPJ numérico válido")
    class CnpjNumericoValido {

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "00000000000191",   // sem máscara — d1=9, d2=1
                "11222333000181",   // sem máscara — d1=8, d2=1
        })
        @DisplayName("CNPJ numérico sem máscara deve ser aceito")
        void cnpjNumericoSemMascara(String cnpj) {
            assertTrue(validator.isValid(cnpj, null));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "00.000.000/0001-91",   // com máscara — d1=9, d2=1
                "11.222.333/0001-81",   // com máscara — d1=8, d2=1
        })
        @DisplayName("CNPJ numérico com máscara deve ser aceito")
        void cnpjNumericoComMascara(String cnpj) {
            assertTrue(validator.isValid(cnpj, null));
        }
    }

    // =========================================================================
    // CNPJ numérico inválidos
    // =========================================================================

    @Nested
    @DisplayName("CNPJ numérico inválido")
    class CnpjNumericoInvalido {

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "00000000000000",   // todos zeros
                "11111111111111",   // todos iguais
        })
        @DisplayName("CNPJ com dígitos uniformes deve ser rejeitado")
        void cnpjDigitosUniformes(String cnpj) {
            assertFalse(validator.isValid(cnpj, null));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @ValueSource(strings = {
                "12345678000100",   // 1º dígito errado (esperado 9, informado 0)
                "00000000000190",   // 2º dígito errado (esperado 1, informado 0)
        })
        @DisplayName("CNPJ com dígito verificador inválido deve ser rejeitado")
        void cnpjDigitoVerificadorInvalido(String cnpj) {
            assertFalse(validator.isValid(cnpj, null));
        }
    }

    // =========================================================================
    // CNPJ alfanumérico válidos (RFB nov/2024)
    // =========================================================================

    @Nested
    @DisplayName("CNPJ alfanumérico válido (RFB nov/2024)")
    class CnpjAlfanumericoValido {

        @Test
        @DisplayName("CNPJ alfanumérico sem máscara deve ser aceito")
        void cnpjAlfanumericoSemMascara() {
            // base=1A2B3C4D5E6F → d1=3, d2=4 (valores char-'0': A=17,B=18,...,F=22)
            assertTrue(validator.isValid("1A2B3C4D5E6F34", null));
        }

        @Test
        @DisplayName("CNPJ alfanumérico com máscara deve ser aceito")
        void cnpjAlfanumericoComMascara() {
            assertTrue(validator.isValid("1A.2B3.C4D/5E6F-34", null));
        }

        @Test
        @DisplayName("CNPJ alfanumérico com letras minúsculas deve ser aceito (normalização)")
        void cnpjAlfanumericoMinusculas() {
            assertTrue(validator.isValid("1a.2b3.c4d/5e6f-34", null));
        }
    }

    // =========================================================================
    // CNPJ alfanumérico inválidos
    // =========================================================================

    @Nested
    @DisplayName("CNPJ alfanumérico inválido")
    class CnpjAlfanumericoInvalido {

        @Test
        @DisplayName("CNPJ alfanumérico com dígito verificador errado deve ser rejeitado")
        void cnpjAlfanumericoDigitoErrado() {
            // base=1A2B3C4D5E6F → correto seria 34, informado 01
            assertFalse(validator.isValid("1A2B3C4D5E6F01", null));
        }

        @Test
        @DisplayName("CNPJ alfanumérico com letra no dígito verificador deve ser rejeitado")
        void cnpjAlfanumericoLetraNoDigitoVerificador() {
            // dígitos verificadores (últimas 2 posições) devem ser sempre numéricos
            assertFalse(validator.isValid("1234567890123A", null));
        }
    }

    // =========================================================================
    // Formato inválido (tamanho ou caracteres incorretos)
    // =========================================================================

    @Nested
    @DisplayName("Formato inválido")
    class FormatoInvalido {

        @ParameterizedTest(name = "[{index}] \"{0}\"")
        @ValueSource(strings = {
                "12345",            // muito curto
                "123456789012345",  // muito longo
                "textoInvalido",    // somente texto
                "123.456.789",      // CPF incompleto
                "00.000.000/0001",  // CNPJ incompleto
        })
        @DisplayName("Entrada com tamanho ou formato incorreto deve ser rejeitada")
        void formatoIncorreto(String entrada) {
            assertFalse(validator.isValid(entrada, null));
        }
    }
}
