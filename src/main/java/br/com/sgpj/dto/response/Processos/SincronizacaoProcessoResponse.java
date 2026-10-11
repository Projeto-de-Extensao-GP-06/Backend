package br.com.sgpj.dto.response.Processos;

public record SincronizacaoProcessoResponse(
        Long processoId,
        String mensagem,
        String status
) {}
