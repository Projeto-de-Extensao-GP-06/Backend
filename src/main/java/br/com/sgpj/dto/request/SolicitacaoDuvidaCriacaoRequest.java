package br.com.sgpj.dto.request;

import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.validation.CpfCnpjValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitacaoDuvidaCriacaoRequest(
        @Schema(description = "Nome completo do solicitante", example = "João da Silva")
        @NotBlank(message = "Nome completo é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nomeCompleto,

        @Schema(description = "Email do solicitante", example = "joao@email.com")
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
        String email,

        @Schema(description = "Indica se o solicitante é pessoa física (true) ou jurídica (false)", example = "true")
        @NotNull(message = "Indicador de pessoa física é obrigatório")
        Boolean isPF,

        @Schema(description = "Telefone do solicitante", example = "11999999999")
        @NotBlank(message = "Telefone é obrigatório")
        @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Schema(description = "Número do processo, quando existir", example = "00000000000000000000")
        @Size(max = 25, message = "Número do processo deve ter no máximo 25 caracteres")
        String numeroProcesso,

        @Schema(description = "Descrição da situação do solicitante")
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @Schema(description = "Como conheceu o escritório", example = "INSTAGRAM")
        @NotNull(message = "Como conheceu é obrigatório")
        ComoConheceuEnum comoConheceu,

        @Schema(description = "Aceite das políticas e termos", example = "true")
        @NotNull(message = "Aceite das políticas e termos é obrigatório")
        @AssertTrue(message = "É necessário aceitar as políticas e termos")
        Boolean aceitouTermos,

        @Schema(description = "Razão social da empresa (Obrigatória para PJ)", example = "João da Silva")
        @Size(max = 150, message = "Razão social deve ter no máximo 150 caracteres")
        String razaoSocial,

        @Schema(description = "CNPJ do solicitante", example = "25523524000198")
        String cnpj
) {

        @JsonIgnore
        @AssertTrue(message = "Razão social enviada é inválida.")
        public boolean isRazaoSocialValida() {

            if (Boolean.TRUE.equals(isPF)) {
                return true;
            }

            return razaoSocial != null && !razaoSocial.isBlank();
        }

        @JsonIgnore
        @AssertTrue(message = "Cnpj enviado é inválido.")
        public boolean isCnpjValido() {

            if (Boolean.TRUE.equals(isPF)) {
                return true;
            }

            if (cnpj == null || cnpj.isBlank()) {
                return false;
            }

            if (cnpj == null || !cnpj.matches("[0-9A-Z]{12}[0-9]{2}")) {
                return false;
            }

            return CpfCnpjValidator.isValidCnpj(cnpj);
        }
}
