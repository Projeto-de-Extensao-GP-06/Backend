package br.com.sgpj.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE      = "sincronizacao.processo.queue";
    public static final String EXCHANGE   = "sincronizacao.processo.exchange";
    public static final String ROUTING_KEY = "sincronizacao.processo.routingkey";

    public static final String DLX        = "sincronizacao.processo.dlx";
    public static final String WAIT_QUEUE = "sincronizacao.processo.wait.queue";
    public static final String DLQ        = "sincronizacao.processo.dlq";

    public static final String ROUTING_KEY_WAIT = "sincronizacao.processo.wait";
    public static final String ROUTING_KEY_DLQ  = "sincronizacao.processo.dlq";

    private final int retryTtlMs;

    public RabbitMQConfig(@Value("${sgpj.sincronizacao.queue.retry-ttl-ms:30000}") int retryTtlMs) {
        this.retryTtlMs = retryTtlMs;
    }

    @Bean
    public DirectExchange sincronizacaoExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange sincronizacaoDeadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    /** Fila durável: rejeições (sem requeue) são roteadas para a fila de espera via DLX. */
    @Bean
    public Queue sincronizacaoQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_WAIT)
                .build();
    }

    /** Fila de espera: após o TTL a mensagem retorna para a fila principal (retry). */
    @Bean
    public Queue sincronizacaoWaitQueue() {
        return QueueBuilder.durable(WAIT_QUEUE)
                .withArgument("x-message-ttl", retryTtlMs)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY)
                .build();
    }

    /** DLQ final: mensagens que esgotaram as tentativas ou falharam de forma não retentável. */
    @Bean
    public Queue sincronizacaoDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding sincronizacaoBinding(Queue sincronizacaoQueue, DirectExchange sincronizacaoExchange) {
        return BindingBuilder
                .bind(sincronizacaoQueue)
                .to(sincronizacaoExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public Binding sincronizacaoWaitQueueBinding(Queue sincronizacaoWaitQueue, DirectExchange sincronizacaoDeadLetterExchange) {
        return BindingBuilder.bind(sincronizacaoWaitQueue).to(sincronizacaoDeadLetterExchange).with(ROUTING_KEY_WAIT);
    }

    @Bean
    public Binding sincronizacaoDeadLetterQueueBinding(Queue sincronizacaoDeadLetterQueue, DirectExchange sincronizacaoDeadLetterExchange) {
        return BindingBuilder.bind(sincronizacaoDeadLetterQueue).to(sincronizacaoDeadLetterExchange).with(ROUTING_KEY_DLQ);
    }

    /** Converte objetos Java ↔ JSON automaticamente nas mensagens */
    @Bean
    @SuppressWarnings({"deprecation", "removal"})
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** RabbitTemplate configurado com o conversor JSON */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
