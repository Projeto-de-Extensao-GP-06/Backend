package br.com.sgpj.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes do DTO ValidarOtpRequest")
class ValidarOtpRequestTest {

    private Validator validator;

    private static final String EMAIL_VALIDO = "john@doe.com";
    private static final String OTP_VALIDO = "123456";

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Deve passar na validação com e-mail e OTP válidos")
    void devePassarNaValidacaoComDadosValidos() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_VALIDO, OTP_VALIDO);

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertEquals(EMAIL_VALIDO, request.email());
        assertEquals(OTP_VALIDO, request.otp());
        assertTrue(violacoes.isEmpty(), "Não deve haver violações de validação para dados válidos");
    }

    @Test
    @DisplayName("Deve falhar na validação quando e-mail for nulo ou em branco")
    void deveFalharNaValidacaoQuandoEmailForNuloOuEmBranco() {
        // Given
        ValidarOtpRequest requestNulo = new ValidarOtpRequest(null, OTP_VALIDO);
        ValidarOtpRequest requestVazio = new ValidarOtpRequest("   ", OTP_VALIDO);

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoesNulo = validator.validate(requestNulo);
        Set<ConstraintViolation<ValidarOtpRequest>> violacoesVazio = validator.validate(requestVazio);

        // Then
        assertFalse(violacoesNulo.isEmpty());
        assertTrue(violacoesNulo.stream().anyMatch(v -> v.getMessage().equals("E-mail é obrigatório")));

        assertFalse(violacoesVazio.isEmpty());
        assertTrue(violacoesVazio.stream().anyMatch(v -> v.getMessage().equals("E-mail é obrigatório")));
    }

    @Test
    @DisplayName("Deve falhar na validação quando o formato do e-mail for inválido")
    void deveFalharNaValidacaoQuandoEmailForInvalido() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest("email_invalido", OTP_VALIDO);

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    @DisplayName("Deve falhar na validação quando OTP for nulo ou em branco")
    void deveFalharNaValidacaoQuandoOtpForNuloOuEmBranco() {
        // Given
        ValidarOtpRequest requestNulo = new ValidarOtpRequest(EMAIL_VALIDO, null);
        ValidarOtpRequest requestVazio = new ValidarOtpRequest(EMAIL_VALIDO, "   ");

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoesNulo = validator.validate(requestNulo);
        Set<ConstraintViolation<ValidarOtpRequest>> violacoesVazio = validator.validate(requestVazio);

        // Then
        assertFalse(violacoesNulo.isEmpty());
        assertTrue(violacoesNulo.stream().anyMatch(v -> v.getMessage().equals("OTP é obrigatória")));

        assertFalse(violacoesVazio.isEmpty());
        assertTrue(violacoesVazio.stream().anyMatch(v -> v.getMessage().equals("OTP é obrigatória")));
    }

    @Test
    @DisplayName("Deve falhar na validação quando OTP tiver tamanho menor que 6")
    void deveFalharNaValidacaoQuandoOtpTamanhoMenorQue6() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_VALIDO, "12345");

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("otp")));
    }

    @Test
    @DisplayName("Deve falhar na validação quando OTP tiver tamanho maior que 6")
    void deveFalharNaValidacaoQuandoOtpTamanhoMaiorQue6() {
        // Given
        ValidarOtpRequest request = new ValidarOtpRequest(EMAIL_VALIDO, "1234567");

        // When
        Set<ConstraintViolation<ValidarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("otp")));
    }
}