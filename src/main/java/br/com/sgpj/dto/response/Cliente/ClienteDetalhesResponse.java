package br.com.sgpj.dto.response.Cliente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record ClienteDetalhesResponse(

        @Schema(description = "Id do cliente", example = "1")
        Long id,

        @Schema(description = "Nome ou razão social do cliente", example = "João da Silva")
        String nomeRazaoSocial,

        @Schema(description = "CPF ou CNPJ do cliente", example = "12345678901")
        String cpfCnpj,

        @Schema(description = "Indica se o cliente é pessoa física (true) ou jurídica (false)", example = "true")
        Boolean isPF,

        @Schema(description = "RG do cliente", example = "123456789")
        String rg,

        @Schema(description = "Como conheceu o escritório", example = "Instagram")
        String comoConheceu,

        @Schema(description = "Status do cliente")
        StatusClienteDto status,

        @Schema(description = "Endereço do cliente")
        EnderecoDto endereco,

        @Schema(description = "Data e hora do envio do formulário", example = "2026-05-16T10:30:00")
        LocalDateTime dataHoraEnvioFormulario,

        @Schema(description = "Data e hora da transformação em cliente", example = "2026-05-16T14:00:00")
        LocalDateTime dataHoraTransformacaoEmCliente,

        @Schema(description = "Contatos do cliente")
        List<ContatoDto> contatos
) {

    public record StatusClienteDto(
            @Schema(description = "Id do status", example = "1")
            Long id,

            @Schema(description = "Nome do status", example = "Ativo")
            String nome
    ) {}

    public record EnderecoDto(
            @Schema(description = "Id do endereço", example = "1")
            Long id,

            @Schema(description = "CEP", example = "01001000")
            String cep,

            @Schema(description = "Logradouro", example = "Praça da Sé")
            String logradouro,

            @Schema(description = "Número", example = "100")
            String numero,

            @Schema(description = "Bairro", example = "Sé")
            String bairro,

            @Schema(description = "Cidade", example = "São Paulo")
            String cidade,

            @Schema(description = "Estado (UF)", example = "SP")
            String estado,

            @Schema(description = "Complemento", example = "Sala 101")
            String complemento
    ) {}

    public record ContatoDto(
            @Schema(description = "Id do contato", example = "1")
            Long id,

            @Schema(description = "Nome do contato", example = "Maria Silva")
            String nome,

            @Schema(description = "Email do contato", example = "maria@email.com")
            String email,

            @Schema(description = "Telefone do contato", example = "(11) 99999-9999")
            String telefone,

            @Schema(description = "Profissão do contato", example = "Advogada")
            String profissao,

            @Schema(description = "Indica se é o contato principal", example = "true")
            Boolean principal
    ) {}
}

