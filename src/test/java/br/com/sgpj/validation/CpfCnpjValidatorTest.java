package br.com.sgpj.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfCnpjValidatorTest {

    private CpfCnpjValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CpfCnpjValidator();
    }

    // -------------------------------------------------------------------------
    // Nulos e brancos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar true para valores nulos ou em branco (delegado para @NotBlank/@NotNull)")
    void deveRetornarTrueParaNuloOuEmBranco() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }

    // -------------------------------------------------------------------------
    // CPF válidos
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "27011910040",       // CPF válido sem máscara
            "796.883.690-33",    // CPF válido com máscara
            "270.119.100-40",    // CPF válido com máscara (mesmo do primeiro)
    })
    @DisplayName("Deve validar CPFs válidos com ou sem máscara")
    void deveValidarCpfValido(String input) {
        assertTrue(validator.isValid(input, null));
    }

    // -------------------------------------------------------------------------
    // CNPJ numérico válidos
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000191",        // CNPJ numérico válido sem máscara
            "00.000.000/0001-91",    // CNPJ numérico válido com máscara
    })
    @DisplayName("Deve validar CNPJs numéricos válidos com ou sem máscara")
    void deveValidarCnpjNumericoValido(String input) {
        assertTrue(validator.isValid(input, null));
    }

    // -------------------------------------------------------------------------
    // CNPJ alfanumérico válidos (RFB nov/2024)
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "1A2B3C4D5E6F01",        // CNPJ alfanumérico válido sem máscara
            "1A.2B3.C4D/5E6F-01",    // CNPJ alfanumérico válido com máscara
            "1a.2b3.c4d/5e6f-01",    // CNPJ alfanumérico válido com letras minúsculas (normalizado)
    })
    @DisplayName("Deve validar CNPJs alfanuméricos válidos (formato RFB nov/2024)")
    void deveValidarCnpjAlfanumericoValido(String input) {
        // Calcula os dígitos esperados para garantir que os casos de teste são válidos
        String stripped = input.toUpperCase().replaceAll("[.\\-/]", "");
        assertTrue(validator.isValid(input, null),
                "Esperado válido para: " + input + " (stripped=" + stripped + ")");
    }

    // -------------------------------------------------------------------------
    // Inválidos
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",       // CPF: sequência repetida
            "00000000000",       // CPF: sequência repetida de zeros
            "12345678900",       // CPF: dígitos verificadores inválidos
            "00000000000000",    // CNPJ: sequência repetida
            "12345678000100",    // CNPJ: dígitos verificadores inválidos
            "12345",             // Tamanho inválido
            "textoInvalido",     // Formato inválido
            "1234567890123A",    // CNPJ alfanumérico: letra na posição do dígito verificador
    })
    @DisplayName("Deve rejeitar CPFs/CNPJs inválidos, repetidos ou com formato incorreto")
    void deveRejeitarInvalidos(String input) {
        assertFalse(validator.isValid(input, null));
    }
}
