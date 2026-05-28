package br.com.sgpj.service;

import br.com.sgpj.dto.response.FaseProcessoResponse;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.mapper.FaseProcessoMapper;
import br.com.sgpj.repository.FaseProcessoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FaseProcessoService {

    private final FaseProcessoRepository faseProcessoRepository;

    public FaseProcessoService(FaseProcessoRepository faseProcessoRepository) {
        this.faseProcessoRepository = faseProcessoRepository;
    }

    public List<FaseProcessoResponse> listarFases(){

        List<FaseProcesso> fasesEncontradas = faseProcessoRepository.findAll();
        List<FaseProcessoResponse> fasesResponse = new ArrayList<>();

        for (FaseProcesso faseProcesso : fasesEncontradas){
            fasesResponse.add(FaseProcessoMapper.toResponse(faseProcesso));
        }

        return fasesResponse;
    }
}
