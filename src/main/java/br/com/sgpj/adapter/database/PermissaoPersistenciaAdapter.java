package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Permissao;
import br.com.sgpj.exception.PermissaoNaoEncontradaException;
import br.com.sgpj.repository.PermissaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class PermissaoPersistenciaAdapter {

    public PermissaoPersistenciaAdapter(PermissaoRepository permissaoRepository) {
        this.permissaoRepository = permissaoRepository;
    }

    private final PermissaoRepository permissaoRepository;

    public Permissao buscarPermissaoPorId(Long permissaoId) {
        return permissaoRepository.findById(permissaoId)
                .orElseThrow(() -> new PermissaoNaoEncontradaException("Permissão não encontrada: " + permissaoId));
    }
}
