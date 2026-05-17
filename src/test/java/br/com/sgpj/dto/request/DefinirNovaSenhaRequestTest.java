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

@DisplayName("Testes do DTO DefinirNovaSenhaRequest")
class DefinirNovaSenhaRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Deve passar na validação com senha dentro dos limites de tamanho")
    void devePassarNaValidacaoComSenhaValida() {
        // Given
        String senhaValida = "senhaSegura123"; // 14 caracteres (entre 6 e 20)
        DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest(senhaValida);

        // When
        Set<ConstraintViolation<DefinirNovaSenhaRequest>> violacoes = validator.validate(request);

        // Then
        assertEquals(senhaValida, request.senha());
        assertTrue(violacoes.isEmpty(), "Não deve haver violações de validação para uma senha válida");
    }

    @Test
    @DisplayName("Deve falhar na validação (@NotBlank) quando senha for nula ou em branco")
    void deveFalharNaValidacaoQuandoSenhaForNulaOuEmBranco() {
        // Given
        DefinirNovaSenhaRequest requestNulo = new DefinirNovaSenhaRequest(null);
        DefinirNovaSenhaRequest requestVazio = new DefinirNovaSenhaRequest("   ");

        // When
        Set<ConstraintViolation<DefinirNovaSenhaRequest>> violacoesNulo = validator.validate(requestNulo);
        Set<ConstraintViolation<DefinirNovaSenhaRequest>> violacoesVazio = validator.validate(requestVazio);

        // Then
        assertFalse(violacoesNulo.isEmpty());
        assertTrue(violacoesNulo.stream().anyMatch(v -> v.getMessage().equals("Senha é obrigatória")));

        assertFalse(violacoesVazio.isEmpty());
        assertTrue(violacoesVazio.stream().anyMatch(v -> v.getMessage().equals("Senha é obrigatória")));
    }

    @Test
    @DisplayName("Deve falhar na validação (@Size) quando senha tiver tamanho menor que 6")
    void deveFalharNaValidacaoQuandoSenhaTamanhoMenorQue6() {
        // Given
        DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("12345");

        // When
        Set<ConstraintViolation<DefinirNovaSenhaRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha")),
                "Deve apontar violação na propriedade 'senha'");
    }

    @Test
    @DisplayName("Deve falhar na validação (@Size) quando senha tiver tamanho maior que 20")
    void deveFalharNaValidacaoQuandoSenhaTamanhoMaiorQue20() {
        // Given
        DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("123456789012345678901");

        // When
        Set<ConstraintViolation<DefinirNovaSenhaRequest>> violacoes = validator.validate(request);

        // Then
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha")),
                "Deve apontar violação na propriedade 'senha'");
    }
}