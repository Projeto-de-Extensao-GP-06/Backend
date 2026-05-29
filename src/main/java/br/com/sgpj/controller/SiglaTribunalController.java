package br.com.sgpj.controller;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.exception.EscopoJwtException;
import br.com.sgpj.service.SiglaTribunalService;
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
@RequestMapping("/siglaTribunais")
@Tag(name = "Sigla tribunal")
public class SiglaTribunalController {

    private final UsuarioService usuarioService;
    public final SiglaTribunalService siglaTribunalService;


    public SiglaTribunalController(UsuarioService usuarioService, SiglaTribunalService siglaTribunalService) {
        this.usuarioService = usuarioService;
        this.siglaTribunalService = siglaTribunalService;
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<SiglaTribunalResponse>> listarTribunais(HttpServletRequest httpRequest){

        if (!usuarioService.isCorrectScope(httpRequest, "GENERAL_USE")) {
            throw new EscopoJwtException("Acesso negado: Este token não é válido para visualização de tribunais.");
        }

        List<SiglaTribunalResponse> tribunais = siglaTribunalService.listarTribunais();

        return ResponseEntity.ok(tribunais);
    }
}
