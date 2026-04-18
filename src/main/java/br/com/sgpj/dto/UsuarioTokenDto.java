package br.com.sgpj.dto;

public record UsuarioTokenDto(
        Long id,
        String nome,
        String email,
        String token
) {}
