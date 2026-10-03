package br.com.sgpj.dto.response;

import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;

public record AutenticacaoUsuarioResponse(
        Usuario usuario,
        UsuarioTokenResponse tokenResponse
) {}
