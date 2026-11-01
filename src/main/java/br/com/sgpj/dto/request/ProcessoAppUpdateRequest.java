package br.com.sgpj.dto.request;

import br.com.sgpj.entity.Processo.StatusProcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProcessoAppUpdateRequest(

        @Pattern(regexp = "^$|^\\d{20}$", message = "O CNJ deve conter exatamente 20 dígitos")
        @Schema(description = "Número de CNJ do processo", example = "10108901220228260292")
        String numeroCnj,

        @NotBlank(message = "Descricao do caso e obrigatoria")
        @Size(max = 10000, message = "A descrição não pode ter mais de 10000 caracteres")
        @Schema(description = "Descricao sobre o processo", example = "Esse processo e sobre divisao de bens")
        String descricaoCaso,

        @Size(max = 10000, message = "A observação não pode ter mais de 10000 caracteres")
        @Schema(description = "observacao sobre o processo", example = "Cliente tem algum laudo")
        String observacao,

        @NotNull(message = "Id da prioridade e obrigatorio")
        @Schema(description = "Id do nivel de prioridade do processo", example = "1")
        Long prioridadeId,

        @NotNull(message = "O status do processo é obrigatório")
        @Schema(description = "Status do processo", example = "ATIVO", allowableValues = {"ATIVO", "SUSPENSO", "ENCERRADO"})
        StatusProcesso status,

        @NotNull(message = "Id da fase do processo e obrigatorio")
        @Schema(description = "Id da fase do processo", example = "1")
        Long faseProcessoId,

        LocalDateTime dataAudiencia,

        @Schema(description = "Valor da causa em reais (R$)", example = "2500.00")
        Double valorCausa,

        @Schema(description = "Id do motivo de suspensão ou encerramento. Obrigatório quando status for SUSPENSO ou ENCERRADO.", example = "1")
        Integer motivoStatusProcessoId,

        @Schema(description = "Justificativa complementar (opcional)", example = "Aguardando documentação do cliente")
        String justificativa,

        @Size(max = 80, message = "A descrição da alteração não pode ter mais de 80 caracteres")
        @Schema(description = "Descrição personalizada da alteração de dados do processo (opcional, máx. 80 caracteres). Quando não informada, é registrada a mensagem padrão.", example = "Altera fase do processo.", maxLength = 80, nullable = true)
        String descricaoAlteracao
) {
}
