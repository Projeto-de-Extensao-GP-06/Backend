package br.com.sgpj.dto.response;

import br.com.sgpj.entity.Usuario;

public record AutenticacaoUsuarioResponse(
        Usuario usuario,
        UsuarioTokenResponse tokenResponse
) {}
