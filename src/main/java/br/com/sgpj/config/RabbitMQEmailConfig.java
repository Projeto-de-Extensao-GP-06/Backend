package br.com.sgpj.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQEmailConfig {

    public static final String EMAIL_EXCHANGE = "sgpj.email.exchange";
    public static final String EMAIL_DLX = "sgpj.email.dlx";

    public static final String EMAIL_QUEUE = "sgpj.email.queue";
    public static final String EMAIL_WAIT_QUEUE = "sgpj.email.wait.queue";
    public static final String EMAIL_DLQ = "sgpj.email.dlq";

    public static final String ROUTING_KEY_SEND = "sgpj.email.send";
    public static final String ROUTING_KEY_WAIT = "sgpj.email.wait";
    public static final String ROUTING_KEY_DLQ = "sgpj.email.dlq";

    private final int retryTtlMs;

    public RabbitMQEmailConfig(@Value("${sgpj.email.queue.retry-ttl-ms:30000}") int retryTtlMs) {
        this.retryTtlMs = retryTtlMs;
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return new DirectExchange(EMAIL_DLX, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_WAIT)
                .build();
    }

    @Bean
    public Queue emailWaitQueue() {
        return QueueBuilder.durable(EMAIL_WAIT_QUEUE)
                .withArgument("x-message-ttl", retryTtlMs)
                .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_SEND)
                .build();
    }

    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public Binding emailQueueBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(ROUTING_KEY_SEND);
    }

    @Bean
    public Binding emailWaitQueueBinding(Queue emailWaitQueue, DirectExchange emailDeadLetterExchange) {
        return BindingBuilder.bind(emailWaitQueue).to(emailDeadLetterExchange).with(ROUTING_KEY_WAIT);
    }

    @Bean
    public Binding emailDeadLetterQueueBinding(Queue emailDeadLetterQueue, DirectExchange emailDeadLetterExchange) {
        return BindingBuilder.bind(emailDeadLetterQueue).to(emailDeadLetterExchange).with(ROUTING_KEY_DLQ);
    }
}
