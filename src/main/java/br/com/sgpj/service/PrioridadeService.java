package br.com.sgpj.service;

import br.com.sgpj.dto.response.PrioridadeResponse;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.repository.PrioridadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrioridadeService {

    private final PrioridadeRepository prioridadeRepository;

    public PrioridadeService(PrioridadeRepository prioridadeRepository) {
        this.prioridadeRepository = prioridadeRepository;
    }

    public List<PrioridadeResponse> listarPrioridades() {
        return prioridadeRepository.findAll()
                .stream()
                .map(prioridade -> new PrioridadeResponse(prioridade.getId(), prioridade.getNome()))
                .toList();
    }
}
