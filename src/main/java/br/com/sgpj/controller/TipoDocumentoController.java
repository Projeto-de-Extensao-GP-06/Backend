package br.com.sgpj.controller;

import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.service.TipoDocumentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tiposDocumento")
@Tag(name = "Tipos documento")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoDocumentoResponse>> listarTiposDocumento() {
        return ResponseEntity.ok(tipoDocumentoService.listarTiposDocumento());
    }
}
