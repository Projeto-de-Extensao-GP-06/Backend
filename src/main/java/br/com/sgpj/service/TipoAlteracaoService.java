package br.com.sgpj.service;

import br.com.sgpj.dto.response.TipoAlteracaoResponse;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoAlteracaoService {

    private final TipoAlteracaoRepository tipoAlteracaoRepository;

    public TipoAlteracaoService(TipoAlteracaoRepository tipoAlteracaoRepository) {
        this.tipoAlteracaoRepository = tipoAlteracaoRepository;
    }

    public List<TipoAlteracaoResponse> listarTiposAlteracao() {
        return tipoAlteracaoRepository.findAll()
                .stream()
                .map(tipoAlteracao -> new TipoAlteracaoResponse(tipoAlteracao.getId(), tipoAlteracao.getNome()))
                .toList();
    }
}
