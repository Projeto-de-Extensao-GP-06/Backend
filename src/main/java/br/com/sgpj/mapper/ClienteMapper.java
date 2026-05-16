package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.ClienteListarResponse;
import br.com.sgpj.entity.Cliente;

import java.util.Collections;
import java.util.List;

public class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteListarResponse toListarDto(Cliente cliente) {
        String email = null;
        if (cliente.getContatos() != null) {
            email = cliente.getContatos().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getPrincipal()))
                    .findFirst()
                    .map(c -> c.getEmail())
                    .orElse(null);
        }

        return new ClienteListarResponse(
                cliente.getId(),
                cliente.getNomeRazaoSocial(),
                cliente.getCpfCnpj(),
                email,
                cliente.getStatusCliente() != null ? cliente.getStatusCliente().getNome() : null,
                cliente.getDataHoraEnvioFormulario(),
                cliente.getDataHoraTransformacaoEmCliente()
        );
    }

    public static ClienteDetalhesResponse toDetalhesDto(Cliente cliente) {
        ClienteDetalhesResponse.StatusClienteDto statusDto = null;
        if (cliente.getStatusCliente() != null) {
            statusDto = new ClienteDetalhesResponse.StatusClienteDto(
                    cliente.getStatusCliente().getId(),
                    cliente.getStatusCliente().getNome()
            );
        }

        ClienteDetalhesResponse.EnderecoDto enderecoDto = null;
        if (cliente.getEndereco() != null) {
            var e = cliente.getEndereco();
            enderecoDto = new ClienteDetalhesResponse.EnderecoDto(
                    e.getId(),
                    e.getCep(),
                    e.getLogradouro(),
                    e.getNumero(),
                    e.getBairro(),
                    e.getCidade(),
                    e.getEstado(),
                    e.getComplemento()
            );
        }

        List<ClienteDetalhesResponse.ContatoDto> contatosDto = Collections.emptyList();
        if (cliente.getContatos() != null) {
            contatosDto = cliente.getContatos().stream()
                    .map(c -> new ClienteDetalhesResponse.ContatoDto(
                            c.getId(),
                            c.getNome(),
                            c.getEmail(),
                            c.getTelefone(),
                            c.getProfissao(),
                            c.getPrincipal()
                    ))
                    .toList();
        }

        return new ClienteDetalhesResponse(
                cliente.getId(),
                cliente.getNomeRazaoSocial(),
                cliente.getCpfCnpj(),
                cliente.getRg(),
                statusDto,
                enderecoDto,
                cliente.getDataHoraEnvioFormulario(),
                cliente.getDataHoraTransformacaoEmCliente(),
                contatosDto
        );
    }
}

