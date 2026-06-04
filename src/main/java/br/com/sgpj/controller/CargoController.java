package br.com.sgpj.controller;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.UsuarioDetalhesResponse;
import br.com.sgpj.service.CargoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Page<CargoResponse>> listarCargosPaginados(@ParameterObject @PageableDefault(
            size = 10,
            page = 0,
            sort = "nome",
            direction = Sort.Direction.ASC
    ) Pageable pageable) {
        return ResponseEntity.ok(cargoService.listarPaginado(pageable));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<CargoResponse> criar(@RequestBody @Valid CriarCargoRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.status(201).body(cargoService.criar(request, email));
    }

    @PutMapping("/{cargoId}/permissoes")
    public ResponseEntity<CargoResponse> atualizarPermissoes(
            @PathVariable Long cargoId,
            @RequestBody @Valid AtribuirPermissoesRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return ResponseEntity.ok(cargoService.atualizarPermissoes(cargoId, request, email));
    }

    @DeleteMapping("/{cargoId}")
    public ResponseEntity<Void> deletar(@PathVariable Long cargoId) {
        cargoService.deletar(cargoId);
        return ResponseEntity.noContent().build();
    }
}
