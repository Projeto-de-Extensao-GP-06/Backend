package br.com.sgpj.controller;

import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.PermissaoService;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissoes")
@Tag(name = "Permissoes")
public class PermissaoController {

    private final UsuarioService usuarioService;
    private final PermissaoService permissaoService;

    public PermissaoController(UsuarioService usuarioService, PermissaoService permissaoService) {
        this.usuarioService = usuarioService;
        this.permissaoService = permissaoService;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<PermissaoResponse>> listarPermissoes(HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de permissões.");
        }


        return ResponseEntity.ok(permissaoService.listarPermissoes());
    }
}
