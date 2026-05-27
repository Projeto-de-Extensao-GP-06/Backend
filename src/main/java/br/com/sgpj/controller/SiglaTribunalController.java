package br.com.sgpj.controller;

import br.com.sgpj.dto.response.SiglaTribunalResponse;
import br.com.sgpj.service.SiglaTribunalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/siglaTribunais")
@Tag(name = "Sigla tribunal")
public class SiglaTribunalController {

    public final SiglaTribunalService siglaTribunalService;


    public SiglaTribunalController(SiglaTribunalService siglaTribunalService) {
        this.siglaTribunalService = siglaTribunalService;
    }

    @GetMapping
    public ResponseEntity<List<SiglaTribunalResponse>> listarTribunais(){

        List<SiglaTribunalResponse> tribunais = siglaTribunalService.listarTribunais();

        return ResponseEntity.ok(tribunais);
    }
}
