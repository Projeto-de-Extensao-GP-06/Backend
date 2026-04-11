package br.com.sgpj.dto;

public record UsuarioTokenDto(
        Long userId,
        String nome,
        String email,
        String token
) {}
