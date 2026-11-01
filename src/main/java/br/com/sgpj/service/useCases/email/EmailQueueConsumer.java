package br.com.sgpj.service.useCases.email;

import br.com.sgpj.config.RabbitMQEmailConfig;
import br.com.sgpj.dto.EmailMessagePayload;
import br.com.sgpj.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmailQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailQueueConsumer.class);

    private final EmailService emailService;
    private final RabbitTemplate rabbitTemplate;
    private final int maxRetries;

    public EmailQueueConsumer(
            EmailService emailService,
            RabbitTemplate rabbitTemplate,
            @Value("${sgpj.email.queue.max-retries:3}") int maxRetries
    ) {
        this.emailService = emailService;
        this.rabbitTemplate = rabbitTemplate;
        this.maxRetries = maxRetries;
    }

    @RabbitListener(queues = RabbitMQEmailConfig.EMAIL_QUEUE)
    public void consumirEmail(EmailMessagePayload payload, Message amqpMessage) {
        long tentativasRealizadas = extrairTentativasXDeath(amqpMessage);

        try {
            emailService.buildEmail(payload.to(), payload.subject(), payload.body());
        } catch (Exception e) {
            if (tentativasRealizadas < maxRetries) {
                log.warn("Falha ao enviar e-mail para '{}' (retentativa {}/{}). Encaminhando para fila de espera (DLX/TTL). Motivo: {}",
                        payload.to(), tentativasRealizadas + 1, maxRetries, e.getMessage());
                throw new AmqpRejectAndDontRequeueException("Falha temporária no envio de e-mail, agendando retry via DLX", e);
            }

            log.error("Limite máximo de {} retentativas atingido para o destinatário '{}'. Encaminhando para a DLQ final ({}). Motivo: {}",
                    maxRetries, payload.to(), RabbitMQEmailConfig.EMAIL_DLQ, e.getMessage(), e);

            rabbitTemplate.convertAndSend(
                    RabbitMQEmailConfig.EMAIL_DLX,
                    RabbitMQEmailConfig.ROUTING_KEY_DLQ,
                    payload
            );
        }
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
            if ((queue == null || RabbitMQEmailConfig.EMAIL_QUEUE.equals(queue)) && count instanceof Number number) {
                return number.longValue();
            }
        }

        return 0L;
    }
}
