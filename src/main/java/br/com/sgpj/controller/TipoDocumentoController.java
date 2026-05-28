package br.com.sgpj.controller;

import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.TipoDocumentoService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tiposDocumento")
@Tag(name = "Tipos documento")
public class TipoDocumentoController {

    private final UsuarioService usuarioService;
    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(UsuarioService usuarioService, TipoDocumentoService tipoDocumentoService) {
        this.usuarioService = usuarioService;
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoDocumentoResponse>> listarTiposDocumento(HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de tipos de documentos.");
        }

        return ResponseEntity.ok(tipoDocumentoService.listarTiposDocumento());
    }
}
