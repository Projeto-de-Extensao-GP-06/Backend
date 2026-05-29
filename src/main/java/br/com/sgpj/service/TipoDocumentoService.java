package br.com.sgpj.service;

import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.repository.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public TipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository) {
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    public List<TipoDocumentoResponse> listarTiposDocumento() {
        return tipoDocumentoRepository.findAll()
                .stream()
                .map(tipoDocumento -> new TipoDocumentoResponse(tipoDocumento.getId(), tipoDocumento.getNome()))
                .toList();
    }
}
