package br.com.sgpj.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida CPF e CNPJ (numérico ou alfanumérico) com ou sem máscara.
 *
 * <p>CPF: 11 dígitos numéricos. Algoritmo Mod11 com pesos 10..2 (1º dígito) e 11..2 (2º dígito).
 *
 * <p>CNPJ numérico: 14 dígitos numéricos. Algoritmo Mod11 com pesos {5,4,3,2,9,8,7,6,5,4,3,2}
 * (1º dígito) e {6,5,4,3,2,9,8,7,6,5,4,3,2} (2º dígito).
 *
 * <p>CNPJ alfanumérico: formato válido a partir de novembro de 2024 (Receita Federal). Os 12
 * primeiros caracteres podem ser [0-9A-Z]; os 2 últimos (dígitos verificadores) são sempre
 * numéricos. O valor de cada caractere no Mod11 é calculado como {@code char - '0'}, o que
 * equivale a 0–9 para dígitos e 17–42 para letras A–Z, conforme especificação da RFB.
 *
 * <p>Valores nulos ou em branco são considerados válidos (delegar para {@code @NotBlank}
 * ou {@code @NotNull}).
 */
public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

    private static final int CPF_LENGTH  = 11;
    private static final int CNPJ_LENGTH = 14;

    private static final int[] CNPJ_WEIGHTS_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] CNPJ_WEIGHTS_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        // Normaliza: maiúsculas e remove separadores ('.', '-', '/')
        String stripped = value.toUpperCase().replaceAll("[.\\-/]", "");

        return switch (stripped.length()) {
            case CPF_LENGTH  -> stripped.matches("\\d{11}")              && isValidCpf(stripped);
            case CNPJ_LENGTH -> stripped.matches("[0-9A-Z]{12}[0-9]{2}") && isValidCnpj(stripped);
            default          -> false;
        };
    }

    // -------------------------------------------------------------------------
    // CPF — Mod11, pesos decrescentes de 10 até 2
    // -------------------------------------------------------------------------

    public static boolean isValidCpf(String cpf) {
        // Rejeita sequências uniformes (ex: "11111111111")
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        if (firstDigit != (cpf.charAt(9) - '0')) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        return secondDigit == (cpf.charAt(10) - '0');
    }

    // -------------------------------------------------------------------------
    // CNPJ — Mod11, suporta numérico e alfanumérico (RFB nov/2024)
    // Valor do char: dígito → 0-9, letra A-Z → 17-42  (char - '0')
    // -------------------------------------------------------------------------

    public static boolean isValidCnpj(String cnpj) {
        // Rejeita sequências uniformes (ex: "00000000000000")
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * CNPJ_WEIGHTS_1[i];
        }
        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        if (firstDigit != (cnpj.charAt(12) - '0')) {
            return false;
        }

        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * CNPJ_WEIGHTS_2[i];
        }
        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);
        return secondDigit == (cnpj.charAt(13) - '0');
    }
}
