package br.com.sgpj.controller;

import br.com.sgpj.dto.response.MotivoStatusProcessoResponse;
import br.com.sgpj.mapper.MotivoStatusProcessoMapper;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Motivo Status Processo")
@Validated
@RequestMapping("/motivosStatusProcesso")
public class MotivoStatusProcessoController {

    private final MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    public MotivoStatusProcessoController(MotivoStatusProcessoRepository motivoStatusProcessoRepository) {
        this.motivoStatusProcessoRepository = motivoStatusProcessoRepository;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<MotivoStatusProcessoResponse>> listarMotivos() {
        List<MotivoStatusProcessoResponse> motivos = motivoStatusProcessoRepository.findAll().stream()
                .map(MotivoStatusProcessoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(motivos);
    }

}
