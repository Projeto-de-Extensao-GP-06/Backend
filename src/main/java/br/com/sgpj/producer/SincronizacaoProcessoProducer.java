package br.com.sgpj.producer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.service.SincronizacaoStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

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

        System.out.println("============================================================");
        System.out.println("[RABBITMQ PRODUCER] 📥 Pedido ENTROU na fila com sucesso!");
        System.out.println("Processo ID: " + processoId + " | Fila: " + RabbitMQConfig.QUEUE);
        System.out.println("============================================================");

        log.info("[PRODUCER] Sincronização do processo {} enviada para a fila RabbitMQ", processoId);

        statusService.notificar(processoId, "ENFILEIRADO", Map.of(
                "processoId", processoId,
                "status", "ENFILEIRADO",
                "mensagem", "Processo entrou na fila do RabbitMQ para sincronização."
        ));
    }
}
