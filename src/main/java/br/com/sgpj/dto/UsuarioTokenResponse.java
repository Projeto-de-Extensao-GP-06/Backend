package br.com.sgpj.dto;

public record UsuarioTokenResponse(
        Long id,
        String nome,
        String email,
        String token
) {}
