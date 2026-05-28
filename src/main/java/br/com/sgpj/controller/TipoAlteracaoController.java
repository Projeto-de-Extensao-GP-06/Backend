package br.com.sgpj.controller;

import br.com.sgpj.dto.response.TipoAlteracaoResponse;
import br.com.sgpj.service.TipoAlteracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tiposAlteracao")
@Tag(name = "Tipos alteracao")
public class TipoAlteracaoController {

    private final TipoAlteracaoService tipoAlteracaoService;

    public TipoAlteracaoController(TipoAlteracaoService tipoAlteracaoService) {
        this.tipoAlteracaoService = tipoAlteracaoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoAlteracaoResponse>> listarTiposAlteracao() {
        return ResponseEntity.ok(tipoAlteracaoService.listarTiposAlteracao());
    }
}
