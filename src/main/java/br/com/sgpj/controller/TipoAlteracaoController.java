package br.com.sgpj.controller;

import br.com.sgpj.dto.response.TipoAlteracaoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.TipoAlteracaoService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tiposAlteracao")
@Tag(name = "Tipos alteracao")
public class TipoAlteracaoController {

    private final UsuarioService usuarioService;
    private final TipoAlteracaoService tipoAlteracaoService;

    public TipoAlteracaoController(UsuarioService usuarioService, TipoAlteracaoService tipoAlteracaoService) {
        this.usuarioService = usuarioService;
        this.tipoAlteracaoService = tipoAlteracaoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoAlteracaoResponse>> listarTiposAlteracao(HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de tipos de alterações.");
        }

        return ResponseEntity.ok(tipoAlteracaoService.listarTiposAlteracao());
    }
}
