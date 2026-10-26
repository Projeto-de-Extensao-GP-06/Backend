package br.com.sgpj.dto;

public record EmailMessagePayload(
        String to,
        String subject,
        String body
) {
}
