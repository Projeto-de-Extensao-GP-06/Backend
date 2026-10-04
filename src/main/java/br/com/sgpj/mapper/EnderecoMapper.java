package br.com.sgpj.mapper;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.Cliente.ClienteDetalhesResponse;
import br.com.sgpj.entity.Endereco;

public final class EnderecoMapper {

    private EnderecoMapper() {}

    public static Endereco toEntity(ClienteCriacaoRequest.EnderecoRequest request) {
        return toEntity(request, null);
    }

    public static Endereco toEntity(ClienteAtualizacaoRequest.EnderecoRequest request) {
        return toEntity(request, null);
    }

    public static Endereco toEntity(ClienteCriacaoRequest.EnderecoRequest request, Endereco endereco) {
        Endereco destino = endereco != null ? endereco : new Endereco();
        return map(destino,
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.bairro(),
                request.cidade(),
                request.estado(),
                request.complemento());
    }

    public static Endereco toEntity(ClienteAtualizacaoRequest.EnderecoRequest request, Endereco endereco) {
        Endereco destino = endereco != null ? endereco : new Endereco();
        return map(destino,
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.bairro(),
                request.cidade(),
                request.estado(),
                request.complemento());
    }

    public static ClienteDetalhesResponse.EnderecoDto toDto(Endereco endereco) {
        if (endereco == null) {
            return null;
        }

        return new ClienteDetalhesResponse.EnderecoDto(
                endereco.getId(),
                endereco.getCep(),
                endereco.getLogradouro(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getComplemento()
        );
    }

    private static Endereco map(Endereco endereco,
                                String cep,
                                String logradouro,
                                String numero,
                                String bairro,
                                String cidade,
                                String estado,
                                String complemento) {
        endereco.setCep(cep);
        endereco.setLogradouro(logradouro);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        endereco.setComplemento(complemento);
        return endereco;
    }
}
