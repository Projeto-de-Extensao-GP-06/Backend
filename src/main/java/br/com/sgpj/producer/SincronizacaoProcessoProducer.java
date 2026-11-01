package br.com.sgpj.producer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.service.SincronizacaoStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Service
public class SincronizacaoProcessoProducer {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoProcessoProducer.class);

    private final RabbitTemplate rabbitTemplate;
    private final SincronizacaoStatusService statusService;

    public SincronizacaoProcessoProducer(RabbitTemplate rabbitTemplate, SincronizacaoStatusService statusService) {
        this.rabbitTemplate = rabbitTemplate;
        this.statusService = statusService;
    }

    public void enviarSolicitacao(Long processoId) {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(processoId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                mensagem
        );

        log.info("[PRODUCER] Sincronização do processo {} enviada para a fila {}", processoId, RabbitMQConfig.QUEUE);

        statusService.notificar(processoId, "ENFILEIRADO", Map.of(
                "processoId", processoId,
                "status", "ENFILEIRADO",
                "mensagem", "Processo entrou na fila do RabbitMQ para sincronização."
        ));
    }

    /**
     * Publica a solicitação somente após o commit da transação corrente, garantindo que o consumer
     * encontre o processo já persistido. Sem transação ativa, publica imediatamente.
     * Falhas de publicação são apenas registradas: o processo já foi persistido e pode ser
     * sincronizado posteriormente.
     */
    public void enviarAposCommit(Long processoId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviarSemPropagarErro(processoId);
                }
            });
        } else {
            enviarSemPropagarErro(processoId);
        }
    }

    private void enviarSemPropagarErro(Long processoId) {
        try {
            enviarSolicitacao(processoId);
        } catch (Exception ex) {
            log.error("[PRODUCER] Falha ao enfileirar sincronização do processo {}: {}", processoId, ex.getMessage(), ex);
        }
    }
}
