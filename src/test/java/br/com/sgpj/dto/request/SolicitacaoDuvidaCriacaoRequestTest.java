package br.com.sgpj.dto.request;

import br.com.sgpj.entity.ComoConheceuEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Testes do DTO SolicitacaoDuvidaCriacaoRequest")
class SolicitacaoDuvidaCriacaoRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Cenários de Pessoa Jurídica (PJ)")
    class CenariosPj {

        @Test
        @DisplayName("Deve passar na validação quando PJ possui razão social e CNPJ numérico válido de 14 dígitos")
        void devePassarValidacaoComPjValidoNumerico() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica sobre contratos",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "12345678000195" // CNPJ numérico válido
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertTrue(violacoes.isEmpty(), "Não deve haver violações para PJ com dados válidos");
        }

        @Test
        @DisplayName("Deve passar na validação quando PJ possui CNPJ alfanumérico válido (novo formato RFB)")
        void devePassarValidacaoComPjValidoAlfanumerico() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica sobre contratos",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "12ABC34501DE35" // CNPJ alfanumérico válido
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertTrue(violacoes.isEmpty(), "Não deve haver violações para PJ com CNPJ alfanumérico válido");
        }

        @Test
        @DisplayName("Deve falhar na validação quando PJ não informar razão social (nula ou em branco)")
        void deveFalharQuandoPjSemRazaoSocial() {
            SolicitacaoDuvidaCriacaoRequest requestNula = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    null,
                    "12345678000195"
            );

            SolicitacaoDuvidaCriacaoRequest requestEmBranco = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "   ",
                    "12345678000195"
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesNula = validator.validate(requestNula);
            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesBranco = validator.validate(requestEmBranco);

            assertFalse(violacoesNula.isEmpty());
            assertTrue(violacoesNula.stream().anyMatch(v -> v.getMessage().contains("Razão social")));

            assertFalse(violacoesBranco.isEmpty());
            assertTrue(violacoesBranco.stream().anyMatch(v -> v.getMessage().contains("Razão social")));
        }

        @Test
        @DisplayName("Deve falhar na validação quando PJ não informar CNPJ (nulo ou em branco)")
        void deveFalharQuandoPjSemCnpj() {
            SolicitacaoDuvidaCriacaoRequest requestNulo = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    null
            );

            SolicitacaoDuvidaCriacaoRequest requestEmBranco = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "   "
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesNulo = validator.validate(requestNulo);
            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesBranco = validator.validate(requestEmBranco);

            assertFalse(violacoesNulo.isEmpty());
            assertTrue(violacoesNulo.stream().anyMatch(v -> v.getMessage().contains("Cnpj enviado é inválido")));

            assertFalse(violacoesBranco.isEmpty());
            assertTrue(violacoesBranco.stream().anyMatch(v -> v.getMessage().contains("Cnpj enviado é inválido")));
        }

        @Test
        @DisplayName("Deve falhar na validação quando PJ informar CNPJ com tamanho inválido ou máscara não normalizada")
        void deveFalharQuandoPjCnpjFormatoInvalido() {
            SolicitacaoDuvidaCriacaoRequest requestCurto = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "12345678" // 8 dígitos
            );

            SolicitacaoDuvidaCriacaoRequest requestComMascara = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "12.345.678/0001" // incompleto (apenas 12 dígitos)
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesCurto = validator.validate(requestCurto);
            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoesMascara = validator.validate(requestComMascara);

            assertFalse(violacoesCurto.isEmpty());
            assertTrue(violacoesCurto.stream().anyMatch(v -> v.getMessage().contains("Cnpj enviado é inválido")));

            assertFalse(violacoesMascara.isEmpty());
            assertTrue(violacoesMascara.stream().anyMatch(v -> v.getMessage().contains("Cnpj enviado é inválido")));
        }

        @Test
        @DisplayName("Deve falhar na validação quando PJ informar CNPJ com dígito verificador inválido")
        void deveFalharQuandoPjCnpjDigitoInvalido() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Carlos Solicitante",
                    "carlos@empresa.com",
                    false,
                    "11988887777",
                    null,
                    "Dúvida jurídica",
                    ComoConheceuEnum.SITE,
                    true,
                    "Empresa Exemplo LTDA",
                    "12345678000100" // 14 dígitos porém dígito verificador inválido
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertFalse(violacoes.isEmpty());
            assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().contains("Cnpj enviado é inválido")));
        }
    }

    @Nested
    @DisplayName("Cenários de Pessoa Física (PF)")
    class CenariosPf {

        @Test
        @DisplayName("Deve passar na validação quando PF informa campos obrigatórios e deixa razão social e CNPJ nulos")
        void devePassarValidacaoPfComCamposPjNulos() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "João da Silva",
                    "joao@email.com",
                    true,
                    "11999998888",
                    null,
                    "Dúvida sobre processo de herança",
                    ComoConheceuEnum.INDICACAO,
                    true,
                    null,
                    null
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertTrue(violacoes.isEmpty(), "Não deve haver violações para PF com campos obrigatórios preenchidos");
        }

        @Test
        @DisplayName("Deve ignorar silenciosamente razão social e CNPJ quando isPF for true, mesmo com valores inválidos")
        void deveIgnorarSilenciosamenteCamposPjParaPf() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "João da Silva",
                    "joao@email.com",
                    true,
                    "11999998888",
                    null,
                    "Dúvida sobre processo",
                    ComoConheceuEnum.INSTAGRAM,
                    true,
                    "Qualquer Razão Social",
                    "cnpjInvalido123" // Inválido, mas deve ser ignorado para PF
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertTrue(violacoes.isEmpty(), "Campos de PJ devem ser ignorados silenciosamente para PF");
        }
    }

    @Nested
    @DisplayName("Validações gerais de campos obrigatórios")
    class ValidacoesGerais {

        @Test
        @DisplayName("Deve falhar na validação quando aceitouTermos for false")
        void deveFalharQuandoNaoAceitouTermos() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Maria Santos",
                    "maria@email.com",
                    true,
                    "11999998888",
                    null,
                    "Descrição válida",
                    ComoConheceuEnum.OUTROS,
                    false,
                    null,
                    null
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertFalse(violacoes.isEmpty());
            assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().contains("aceitar as políticas e termos")));
        }

        @Test
        @DisplayName("Deve falhar na validação quando email for em formato inválido")
        void deveFalharComEmailInvalido() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Maria Santos",
                    "email-invalido",
                    true,
                    "11999998888",
                    null,
                    "Descrição válida",
                    ComoConheceuEnum.OUTROS,
                    true,
                    null,
                    null
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertFalse(violacoes.isEmpty());
            assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().contains("Email deve ser válido")));
        }

        @Test
        @DisplayName("Deve falhar na validação quando isPF for nulo")
        void deveFalharComIsPfNulo() {
            SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                    "Maria Santos",
                    "maria@email.com",
                    null,
                    "11999998888",
                    null,
                    "Descrição válida",
                    ComoConheceuEnum.OUTROS,
                    true,
                    null,
                    null
            );

            Set<ConstraintViolation<SolicitacaoDuvidaCriacaoRequest>> violacoes = validator.validate(request);

            assertFalse(violacoes.isEmpty());
            assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().contains("Indicador de pessoa física é obrigatório")));
        }
    }
}
