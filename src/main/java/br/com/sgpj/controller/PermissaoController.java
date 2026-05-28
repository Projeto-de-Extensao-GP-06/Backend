package br.com.sgpj.controller;

import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.service.PermissaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permissoes")
@Tag(name = "Permissoes")
public class PermissaoController {

    private final PermissaoService permissaoService;

    public PermissaoController(PermissaoService permissaoService) {
        this.permissaoService = permissaoService;
    }

    @GetMapping
    public ResponseEntity<List<PermissaoResponse>> listarPermissoes() {
        return ResponseEntity.ok(permissaoService.listarPermissoes());
    }
}
