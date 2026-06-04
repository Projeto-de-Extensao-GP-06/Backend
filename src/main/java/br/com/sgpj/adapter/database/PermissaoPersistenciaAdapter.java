package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Permissao;
import br.com.sgpj.exception.PermissaoNaoEncontradaException;
import br.com.sgpj.repository.PermissaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissaoPersistenciaAdapter {

    private final PermissaoRepository permissaoRepository;

    public Permissao buscarPermissaoPorId(Long permissaoId) {
        return permissaoRepository.findById(permissaoId)
                .orElseThrow(() -> new PermissaoNaoEncontradaException("Permissão não encontrada: " + permissaoId));
    }
}
