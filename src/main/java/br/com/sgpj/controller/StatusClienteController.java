package br.com.sgpj.controller;

import br.com.sgpj.dto.response.StatusClienteResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.StatusClienteService;
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
@RequestMapping("/statusClientes")
@Tag(name = "Status clientes")
public class StatusClienteController {

    private final UsuarioService usuarioService;
    private final StatusClienteService statusClienteService;

    public StatusClienteController(UsuarioService usuarioService, StatusClienteService statusClienteService) {
        this.usuarioService = usuarioService;
        this.statusClienteService = statusClienteService;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<StatusClienteResponse>> listarStatusClientes(HttpServletRequest httpRequest) {

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de tipos de status.");
        }


        return ResponseEntity.ok(statusClienteService.listarStatusClientes());
    }
}
