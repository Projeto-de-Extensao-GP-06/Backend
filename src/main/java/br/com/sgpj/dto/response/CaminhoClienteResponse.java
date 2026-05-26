package br.com.sgpj.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Retorna o caminho de armazenamento do cliente para o front-end.
 * O front usa o 'id' para chamar os endpoints do OneDriveController.
 *
 * @param id          ID da tabela padrao_caminho_armazenamento (usado no OneDriveController)
 * @param descricao   Descrição legível: "NomeCliente - CpfOuCnpj"
 * @param caminho     Caminho real no OneDrive (ex: "rafael-silva-12345678901")
 */
public record CaminhoClienteResponse(
        @Schema(description = "ID do caminho de armazenamento", example = "1")
        Long id,

        @Schema(description = "Descrição legível do caminho (NomeCliente - CpfOuCnpj)", example = "Rafael Silva - 12345678901")
        String descricao,

        @Schema(description = "Caminho real no OneDrive", example = "rafael-silva-12345678901")
        String caminho
) {}
