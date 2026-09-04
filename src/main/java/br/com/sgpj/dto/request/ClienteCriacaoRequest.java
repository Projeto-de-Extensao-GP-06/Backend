package br.com.sgpj.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import br.com.sgpj.validation.CpfCnpj;

import java.util.List;

public record ClienteCriacaoRequest(

        @Schema(description = "Nome ou razão social do cliente", example = "João da Silva")
        @NotBlank(message = "Nome/Razão social é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nomeRazaoSocial,

        @Schema(description = "CPF ou CNPJ do cliente (somente números)", example = "12345678901")
        @NotBlank(message = "CPF/CNPJ é obrigatório")
        @CpfCnpj(message = "CPF ou CNPJ inválido")
        @Size(max = 14, message = "CPF/CNPJ deve ter no máximo 14 caracteres")
        String cpfCnpj,

        @Schema(description = "Indica se o solicitante é pessoa física (true) ou jurídica (false)", example = "true")
        @NotNull(message = "Indicador de pessoa física é obrigatório")
        Boolean isPF,

        @Schema(description = "RG do cliente", example = "123456789")
        @Size(max = 15, message = "RG deve ter no máximo 15 caracteres")
        String rg,

        @Schema(description = "ID do status do cliente", example = "1")
        @NotNull(message = "Status do cliente é obrigatório")
        Long statusClienteId,

        @Schema(description = "Endereço do cliente")
        @Valid
        EnderecoRequest endereco,

        @Schema(description = "Contatos do cliente")
        @Valid
        List<ContatoRequest> contatos
) {

    public record EnderecoRequest(
            @Schema(description = "CEP", example = "01001000")
            @Size(max = 10)
            String cep,

            @Schema(description = "Logradouro", example = "Praça da Sé")
            @NotBlank(message = "Logradouro é obrigatório")
            @Size(max = 150)
            String logradouro,

            @Schema(description = "Número", example = "100")
            @NotBlank(message = "Número é obrigatório")
            @Size(max = 10)
            String numero,

            @Schema(description = "Bairro", example = "Sé")
            @NotBlank(message = "Bairro é obrigatório")
            @Size(max = 100)
            String bairro,

            @Schema(description = "Cidade", example = "São Paulo")
            @NotBlank(message = "Cidade é obrigatória")
            @Size(max = 100)
            String cidade,

            @Schema(description = "Estado (UF)", example = "SP")
            @NotBlank(message = "Estado é obrigatório")
            @Size(max = 2)
            String estado,

            @Schema(description = "Complemento", example = "Sala 101")
            @Size(max = 150)
            String complemento
    ) {}

    public record ContatoRequest(
            @Schema(description = "Nome do contato", example = "Maria Silva")
            @NotBlank(message = "Nome do contato é obrigatório")
            @Size(max = 150)
            String nome,

            @Schema(description = "Email do contato", example = "maria@email.com")
            @NotBlank(message = "Email do contato é obrigatório")
            @Email(message = "Email do contato deve ser válido")
            @Size(max = 255)
            String email,

            @Schema(description = "Telefone do contato", example = "(11) 99999-9999")
            @NotBlank(message = "Telefone do contato é obrigatório")
            @Size(max = 20)
            String telefone,

            @Schema(description = "Profissão do contato", example = "Advogada")
            @Size(max = 100)
            String profissao,

            @Schema(description = "Indica se é o contato principal", example = "true")
            Boolean principal
    ) {}
}

