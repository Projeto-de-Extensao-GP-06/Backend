package br.com.sgpj.dto.request;

import br.com.sgpj.entity.Processo.StatusProcesso;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessoAppUpdateRequest - validação de descricaoAlteracao")
class ProcessoAppUpdateRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private ProcessoAppUpdateRequest request(String descricaoAlteracao) {
        return new ProcessoAppUpdateRequest(
                null, "Desc", null, 1L, StatusProcesso.ATIVO, 1L, null, null, null, null, descricaoAlteracao);
    }

    @Test
    @DisplayName("Deve aceitar descrição nula (campo opcional)")
    void deveAceitarNula() {
        assertTrue(validator.validate(request(null)).isEmpty());
    }

    @Test
    @DisplayName("Deve aceitar descrição com exatamente 80 caracteres")
    void deveAceitar80Caracteres() {
        assertTrue(validator.validate(request("a".repeat(80))).isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar descrição com mais de 80 caracteres")
    void deveRejeitarMaisDe80Caracteres() {
        var violacoes = validator.validate(request("a".repeat(81)));

        assertTrue(violacoes.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("descricaoAlteracao")));
    }
}
