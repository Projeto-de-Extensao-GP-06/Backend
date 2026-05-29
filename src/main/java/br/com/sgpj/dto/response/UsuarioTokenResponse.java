 package br.com.sgpj.dto.response;

public record UsuarioTokenResponse(
        Long id,
        String nome,
        String email,
        String token
) {}
