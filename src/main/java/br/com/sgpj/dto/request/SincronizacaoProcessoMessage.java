package br.com.sgpj.dto.request;

import java.io.Serializable;

/**
 * Mensagem publicada na fila RabbitMQ para sincronização assíncrona
 * de dados de um processo com a API DataJud.
 */
public record SincronizacaoProcessoMessage(
        Long processoId
) implements Serializable {}
