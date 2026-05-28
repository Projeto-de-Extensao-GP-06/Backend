package br.com.sgpj.service;

import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.Permissao;
import br.com.sgpj.repository.PermissaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissaoService {

    private final PermissaoRepository permissaoRepository;

    public PermissaoService(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    public List<PermissaoResponse> listarPermissoes() {
        return permissaoRepository.findAll()
                .stream()
                .map(permissao -> new PermissaoResponse(permissao.getId(), permissao.getNome()))
                .toList();
    }
}
