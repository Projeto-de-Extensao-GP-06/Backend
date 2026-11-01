package br.com.sgpj.consumer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.service.SincronizacaoStatusService;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Consome solicitações de sincronização com o DataJud. O retry é de responsabilidade exclusiva
 * do RabbitMQ (fila de espera com TTL via DLX); nenhuma outra camada repete a consulta.
 */
@Service
public class SincronizacaoProcessoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoProcessoConsumer.class);

    private final UpdateProcesso updateProcesso;
    private final SincronizacaoStatusService statusService;
    private final RabbitTemplate rabbitTemplate;
    private final int maxRetries;

    public SincronizacaoProcessoConsumer(UpdateProcesso updateProcesso,
                                         SincronizacaoStatusService statusService,
                                         RabbitTemplate rabbitTemplate,
                                         @Value("${sgpj.sincronizacao.queue.max-retries:3}") int maxRetries) {
        this.updateProcesso = updateProcesso;
        this.statusService = statusService;
        this.rabbitTemplate = rabbitTemplate;
        this.maxRetries = maxRetries;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void processarSincronizacao(SincronizacaoProcessoMessage mensagem, Message amqpMessage) {
        Long processoId = mensagem.processoId();
        long tentativasRealizadas = extrairTentativasXDeath(amqpMessage);

        log.info("[CONSUMER] Sincronização do processo ID {} recebida (tentativa {}/{}).",
                processoId, tentativasRealizadas + 1, maxRetries + 1);

        statusService.notificar(processoId, "PROCESSANDO", Map.of(
                "processoId", processoId,
                "status", "PROCESSANDO",
                "mensagem", "Buscando dados oficiais do processo no DataJud..."
        ));

        try {
            updateProcesso.atualizarDadosDoDataJud(processoId);

            log.info("[CONSUMER] Sincronização do processo ID {} concluída com sucesso via DataJud.", processoId);
            statusService.notificar(processoId, "CONCLUIDO", Map.of(
                    "processoId", processoId,
                    "status", "SUCESSO",
                    "mensagem", "Dados oficiais do processo sincronizados com sucesso."
            ));
        } catch (ObjetoNaoEncontradoException | OperacaoInvalidaException ex) {
            // Erro de negócio: repetir não resolve, vai direto para a DLQ.
            log.error("[CONSUMER] Falha não retentável ao sincronizar o processo ID {}: {}", processoId, ex.getMessage());
            encaminharParaDlq(mensagem);
            notificarErro(processoId, ex.getMessage());
        } catch (Exception ex) {
            if (tentativasRealizadas < maxRetries) {
                log.warn("[CONSUMER] Falha ao sincronizar o processo ID {} (retentativa {}/{}). Encaminhando para fila de espera (DLX/TTL). Motivo: {}",
                        processoId, tentativasRealizadas + 1, maxRetries, ex.getMessage());
                statusService.notificar(processoId, "REPROCESSANDO", Map.of(
                        "processoId", processoId,
                        "status", "REPROCESSANDO",
                        "mensagem", "Falha ao consultar o DataJud. Nova tentativa " + (tentativasRealizadas + 1) + "/" + maxRetries + " agendada."
                ));
                throw new AmqpRejectAndDontRequeueException("Falha temporária na sincronização, agendando retry via DLX", ex);
            }

            log.error("[CONSUMER] Limite de {} retentativas atingido para o processo ID {}. Encaminhando para a DLQ ({}). Motivo: {}",
                    maxRetries, processoId, RabbitMQConfig.DLQ, ex.getMessage(), ex);
            encaminharParaDlq(mensagem);
            notificarErro(processoId, "Não foi possível sincronizar com o DataJud após " + maxRetries + " retentativas.");
        }
    }

    private void encaminharParaDlq(SincronizacaoProcessoMessage mensagem) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.DLX, RabbitMQConfig.ROUTING_KEY_DLQ, mensagem);
    }

    private void notificarErro(Long processoId, String motivo) {
        statusService.notificar(processoId, "CONCLUIDO", Map.of(
                "processoId", processoId,
                "status", "ERRO",
                "mensagem", motivo != null ? motivo : "Falha ao sincronizar com o DataJud."
        ));
    }

    private long extrairTentativasXDeath(Message amqpMessage) {
        if (amqpMessage == null || amqpMessage.getMessageProperties() == null) {
            return 0L;
        }

        List<Map<String, ?>> xDeathHeader = amqpMessage.getMessageProperties().getXDeathHeader();
        if (xDeathHeader == null || xDeathHeader.isEmpty()) {
            return 0L;
        }

        for (Map<String, ?> deathEntry : xDeathHeader) {
            Object queue = deathEntry.get("queue");
            Object count = deathEntry.get("count");
            if ((queue == null || RabbitMQConfig.QUEUE.equals(queue)) && count instanceof Number number) {
                return number.longValue();
            }
        }

        return 0L;
    }
}
