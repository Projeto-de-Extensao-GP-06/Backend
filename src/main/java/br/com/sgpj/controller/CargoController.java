package br.com.sgpj.controller;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.request.RenomearCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.service.CargoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/cargos")
@Tag(name = "Cargos")
public class CargoController {

    public CargoController(CargoService cargoService) {
        this.cargoService = cargoService;
    }

    private final CargoService cargoService;

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Lista os cargos e suas permissões de forma paginada")
    public ResponseEntity<Page<CargoResponse>> listarCargosPaginados(@ParameterObject @PageableDefault(
            sort = "nome",
            direction = Sort.Direction.ASC
    ) Pageable pageable) {
        return ResponseEntity.ok(cargoService.listarPaginado(pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR CARGO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Cria cargo sem permissões atribuídas")
    public ResponseEntity<CargoResponse> criar(@RequestBody @Valid CriarCargoRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();

        return ResponseEntity.status(201).body(cargoService.criar(request, email));
    }

    @PutMapping("/{cargoId}/permissoes")
    @PreAuthorize("hasAuthority('EDITAR CARGO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Atualiza a lista de permissões de um cargo")
    public ResponseEntity<CargoResponse> atualizarPermissoes(
            @PathVariable Long cargoId,
            @RequestBody @Valid AtribuirPermissoesRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                        .getName();

        return ResponseEntity.ok(cargoService.atualizarPermissoes(cargoId, request, email));
    }

    @PutMapping("/{cargoId}")
    @PreAuthorize("hasAuthority('EDITAR CARGO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Renomeia um cargo")
    public ResponseEntity<CargoResponse> renomear(
            @PathVariable Long cargoId,
            @RequestBody @Valid RenomearCargoRequest request) {
        return ResponseEntity.ok(cargoService.renomear(cargoId, request));
    }

    @DeleteMapping("/{cargoId}")
    @PreAuthorize("hasAuthority('EXCLUIR CARGO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    @Operation(summary = "Deleta um cargo e atribui o cargo 'Sem Cargo' aos usuários associados ao cargo excluído")
    public ResponseEntity<Void> deletar(@PathVariable Long cargoId) {
        cargoService.deletar(cargoId);
        return ResponseEntity.noContent().build();
    }
}
