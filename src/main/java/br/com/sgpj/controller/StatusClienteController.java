package br.com.sgpj.controller;

import br.com.sgpj.dto.response.StatusClienteResponse;
import br.com.sgpj.service.StatusClienteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statusClientes")
@Tag(name = "Status clientes")
public class StatusClienteController {

    private final StatusClienteService statusClienteService;

    public StatusClienteController(StatusClienteService statusClienteService) {
        this.statusClienteService = statusClienteService;
    }

    @GetMapping
    public ResponseEntity<List<StatusClienteResponse>> listarStatusClientes() {
        return ResponseEntity.ok(statusClienteService.listarStatusClientes());
    }
}
