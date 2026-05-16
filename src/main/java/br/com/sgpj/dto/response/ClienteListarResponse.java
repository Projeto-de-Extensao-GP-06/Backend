package br.com.sgpj.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ClienteListarResponse(

        @Schema(description = "Id do cliente", example = "1")
        Long id,

        @Schema(description = "Nome ou razão social do cliente", example = "João da Silva")
        String nomeRazaoSocial,

        @Schema(description = "CPF ou CNPJ do cliente", example = "12345678901")
        String cpfCnpj,

        @Schema(description = "Email do contato principal", example = "joao@email.com")
        String email,

        @Schema(description = "Nome do status do cliente", example = "Ativo")
        String statusNome,

        @Schema(description = "Data e hora do envio do formulário", example = "2026-05-16T10:30:00")
        LocalDateTime dataHoraEnvioFormulario,

        @Schema(description = "Data e hora da transformação em cliente", example = "2026-05-16T14:00:00")
        LocalDateTime dataHoraTransformacaoEmCliente
) {}

