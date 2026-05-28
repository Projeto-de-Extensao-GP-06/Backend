package br.com.sgpj.service;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.mapper.SiglaTribunalMapper;
import br.com.sgpj.repository.SiglaTribunalRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SiglaTribunalService {

    private final SiglaTribunalRepository siglaTribunalRepository;

    public SiglaTribunalService(SiglaTribunalRepository siglaTribunalRepository) {
        this.siglaTribunalRepository = siglaTribunalRepository;
    }

    public List<SiglaTribunalResponse> listarTribunais(){

        List<SiglaTribunal> tribunaisEncontrados = siglaTribunalRepository.findAll();
        List<SiglaTribunalResponse> tribunaisResponse = new ArrayList<>();

        for (SiglaTribunal siglaTribunal : tribunaisEncontrados){
            tribunaisResponse.add(SiglaTribunalMapper.toResponse(siglaTribunal));
        }

        return tribunaisResponse;
    }
}
