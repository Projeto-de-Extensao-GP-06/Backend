package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.dto.response.Cliente.ClienteListarResponse;
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
                cliente.getPF(),
                email,
                cliente.getStatusCliente() != null ? cliente.getStatusCliente().getNome() : null,
                cliente.getComoConheceu(),
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

        ClienteDetalhesResponse.EnderecoDto enderecoDto = EnderecoMapper.toDto(cliente.getEndereco());

        List<ClienteDetalhesResponse.ContatoDto> contatosDto = Collections.emptyList();
        if (cliente.getContatos() != null) {
            contatosDto = cliente.getContatos().stream()
                    .map(ContatoMapper::toDto)
                    .toList();
        }

        return new ClienteDetalhesResponse(
                cliente.getId(),
                cliente.getNomeRazaoSocial(),
                cliente.getCpfCnpj(),
                cliente.getPF(),
                cliente.getRg(),
                cliente.getComoConheceu(),
                statusDto,
                enderecoDto,
                cliente.getDataHoraEnvioFormulario(),
                cliente.getDataHoraTransformacaoEmCliente(),
                contatosDto
        );
    }
}

