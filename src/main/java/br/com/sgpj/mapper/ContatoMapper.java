package br.com.sgpj.mapper;

import br.com.sgpj.dto.request.ClienteAtualizacaoRequest;
import br.com.sgpj.dto.request.ClienteCriacaoRequest;
import br.com.sgpj.dto.response.ClienteDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;

public final class ContatoMapper {

    private ContatoMapper() {}

    public static Contato toEntity(ClienteCriacaoRequest.ContatoRequest request, Cliente cliente) {
        return toEntity(request.nome(), request.email(), request.telefone(), request.profissao(), request.principal(), cliente);
    }

    public static Contato toEntity(ClienteAtualizacaoRequest.ContatoRequest request, Cliente cliente) {
        return toEntity(request.nome(), request.email(), request.telefone(), request.profissao(), request.principal(), cliente);
    }

    public static Contato toEntity(String nome, String email, String telefone, String profissao, Boolean principal, Cliente cliente) {
        Contato contato = new Contato();
        contato.setNome(nome);
        contato.setEmail(email);
        contato.setTelefone(telefone);
        contato.setProfissao(profissao);
        contato.setPrincipal(principal);
        contato.setCliente(cliente);
        return contato;
    }

    public static ClienteDetalhesResponse.ContatoDto toDto(Contato contato) {
        if (contato == null) {
            return null;
        }

        return new ClienteDetalhesResponse.ContatoDto(
                contato.getId(),
                contato.getNome(),
                contato.getEmail(),
                contato.getTelefone(),
                contato.getProfissao(),
                contato.getPrincipal()
        );
    }
}
