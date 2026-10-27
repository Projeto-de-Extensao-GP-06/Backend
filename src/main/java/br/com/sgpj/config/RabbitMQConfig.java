package br.com.sgpj.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE      = "sincronizacao.processo.queue";
    public static final String EXCHANGE   = "sincronizacao.processo.exchange";
    public static final String ROUTING_KEY = "sincronizacao.processo.routingkey";

    /** Fila durável: sobrevive a restart do RabbitMQ */
    @Bean
    public Queue sincronizacaoQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange sincronizacaoExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding sincronizacaoBinding(Queue sincronizacaoQueue, DirectExchange sincronizacaoExchange) {
        return BindingBuilder
                .bind(sincronizacaoQueue)
                .to(sincronizacaoExchange)
                .with(ROUTING_KEY);
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
