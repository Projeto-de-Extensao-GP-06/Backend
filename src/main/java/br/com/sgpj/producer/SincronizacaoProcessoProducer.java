package br.com.sgpj.producer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SincronizacaoProcessoProducer {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoProcessoProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public SincronizacaoProcessoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviarSolicitacao(Long processoId) {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(processoId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                mensagem
        );
        log.info("[PRODUCER] Sincronização do processo {} enviada para a fila RabbitMQ", processoId);
    }
}
