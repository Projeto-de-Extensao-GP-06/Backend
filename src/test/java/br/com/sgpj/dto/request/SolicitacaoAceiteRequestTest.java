package br.com.sgpj.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SolicitacaoAceiteRequest validation")
class SolicitacaoAceiteRequestTest {

    private static Validator validator;
    private static AutoCloseable validatorFactory;

    @BeforeAll
    static void setUpValidator() {
        var factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        validatorFactory = factory::close;
    }

    @AfterAll
    static void closeValidator() throws Exception {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Aceita dados válidos de PF com endereço e processo")
    void aceitaRequestValido() {
        SolicitacaoAceiteRequest request = new SolicitacaoAceiteRequest(
                new SolicitacaoAceiteRequest.Client(
                        "PF", "Maria Silva", null, "maria@example.com", "11999999999",
                        "52998224725", null, null, "Advogada",
                        new SolicitacaoAceiteRequest.Address("01001000", "Praça da Sé", "100", null,
                                "Sé", "São Paulo", "SP"), List.of()),
                new SolicitacaoAceiteRequest.Process(null, "média", "Inicial", null, null, null));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Rejeita CPF inválido e audiência sem data e hora completas")
    void rejeitaDocumentoEAudienciaInvalidos() {
        SolicitacaoAceiteRequest request = new SolicitacaoAceiteRequest(
                new SolicitacaoAceiteRequest.Client(
                        "PF", "Maria Silva", null, null, null, "11111111111", null, null, null,
                        new SolicitacaoAceiteRequest.Address("01001000", "Praça da Sé", "100", null,
                                "Sé", "São Paulo", "SP"), List.of()),
                new SolicitacaoAceiteRequest.Process(null, "média", "Inicial",
                        java.time.LocalDate.of(2026, 10, 18), null, null));

        assertFalse(validator.validate(request).isEmpty());
    }
}