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

@DisplayName("Testes do DTO EnviarOtpRequest")
class EnviarOtpRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Deve instanciar corretamente e passar na validação com e-mail válido")
    void devePassarNaValidacaoComEmailValido() {
        // Given
        String emailValido = "john@doe.com";
        EnviarOtpRequest request = new EnviarOtpRequest(emailValido);

        // When
        Set<ConstraintViolation<EnviarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertEquals(emailValido, request.email());
        assertTrue(violacoes.isEmpty(), "Não deve haver violações de validação para um e-mail válido");
    }

    @Test
    @DisplayName("Deve falhar na validação (@NotBlank) quando e-mail for nulo")
    void deveFalharNaValidacaoQuandoEmailForNulo() {
        // Given
        EnviarOtpRequest request = new EnviarOtpRequest(null);

        // When
        Set<ConstraintViolation<EnviarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        boolean contemMensagemEsperada = violacoes.stream()
                .anyMatch(v -> v.getMessage().equals("E-mail é obrigatório"));
        assertTrue(contemMensagemEsperada, "Deve conter a mensagem personalizada de e-mail obrigatório");
    }

    @Test
    @DisplayName("Deve falhar na validação (@NotBlank) quando e-mail for vazio ou em branco")
    void deveFalharNaValidacaoQuandoEmailForVazioOuEmBranco() {
        // Given
        EnviarOtpRequest request = new EnviarOtpRequest("   ");

        // When
        Set<ConstraintViolation<EnviarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        boolean contemMensagemEsperada = violacoes.stream()
                .anyMatch(v -> v.getMessage().equals("E-mail é obrigatório"));
        assertTrue(contemMensagemEsperada, "Deve conter a mensagem personalizada de e-mail obrigatório");
    }

    @Test
    @DisplayName("Deve falhar na validação (@Email) quando o formato do e-mail for inválido")
    void deveFalharNaValidacaoQuandoEmailForInvalido() {
        // Given
        EnviarOtpRequest request = new EnviarOtpRequest("email-sem-arroba-e-dominio");

        // When
        Set<ConstraintViolation<EnviarOtpRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        boolean violacaoNoCampoEmail = violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertTrue(violacaoNoCampoEmail, "A violação deve ocorrer no campo 'email'");
    }
}