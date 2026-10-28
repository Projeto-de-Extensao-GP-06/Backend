package br.com.sgpj.service.useCases.email;

import br.com.sgpj.config.RabbitMQEmailConfig;
import br.com.sgpj.dto.EmailMessagePayload;
import br.com.sgpj.exception.EmailSendException;
import br.com.sgpj.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EmailQueueConsumer")
class EmailQueueConsumerTest {

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private EmailQueueConsumer consumer;

    private static final int MAX_RETRIES = 3;

    @BeforeEach
    void setUp() {
        consumer = new EmailQueueConsumer(emailService, rabbitTemplate, MAX_RETRIES);
    }

    @Test
    @DisplayName("Deve processar mensagem e enviar email via SMTP quando não houver erro")
    void deveProcessarMensagemComSucesso() {
        EmailMessagePayload payload = new EmailMessagePayload("adv@sgpj.com.br", "Assunto", "<p>Corpo</p>");
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        consumer.consumirEmail(payload, amqpMessage);

        verify(emailService).buildEmail("adv@sgpj.com.br", "Assunto", "<p>Corpo</p>");
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve rejeitar mensagem sem requeue (acionando DLX/TTL) na primeira falha sem x-death")
    void deveRejeitarParaDlxNaPrimeiraFalha() {
        EmailMessagePayload payload = new EmailMessagePayload("adv@sgpj.com.br", "Assunto", "<p>Corpo</p>");
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        doThrow(new EmailSendException("Timeout SMTP"))
                .when(emailService)
                .buildEmail(payload.to(), payload.subject(), payload.body());

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.consumirEmail(payload, amqpMessage));

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve rejeitar mensagem sem requeue (acionando DLX/TTL) quando x-death count for menor que maxRetries")
    void deveRejeitarParaDlxQuandoTentativasMenoresQueLimite() {
        EmailMessagePayload payload = new EmailMessagePayload("adv@sgpj.com.br", "Assunto", "<p>Corpo</p>");
        MessageProperties props = new MessageProperties();
        props.setHeader("x-death", List.of(Map.of("queue", RabbitMQEmailConfig.EMAIL_QUEUE, "count", 2L)));
        Message amqpMessage = new Message(new byte[0], props);

        doThrow(new EmailSendException("Timeout SMTP"))
                .when(emailService)
                .buildEmail(payload.to(), payload.subject(), payload.body());

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.consumirEmail(payload, amqpMessage));

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve encaminhar mensagem para a DLQ final quando x-death count atingir maxRetries")
    void deveEncaminharParaDlqFinalQuandoAtingirLimiteDeRetentativas() {
        EmailMessagePayload payload = new EmailMessagePayload("adv@sgpj.com.br", "Assunto", "<p>Corpo</p>");
        MessageProperties props = new MessageProperties();
        props.setHeader("x-death", List.of(Map.of("queue", RabbitMQEmailConfig.EMAIL_QUEUE, "count", 3L)));
        Message amqpMessage = new Message(new byte[0], props);

        doThrow(new EmailSendException("Erro permanente SMTP"))
                .when(emailService)
                .buildEmail(payload.to(), payload.subject(), payload.body());

        assertDoesNotThrow(() -> consumer.consumirEmail(payload, amqpMessage));

        verify(rabbitTemplate).convertAndSend(
                RabbitMQEmailConfig.EMAIL_DLX,
                RabbitMQEmailConfig.ROUTING_KEY_DLQ,
                payload
        );
    }

    @Test
    @DisplayName("Deve desserializar corretamente byte[] JSON para EmailMessagePayload via MessagingMessageListenerAdapter")
    @SuppressWarnings({"deprecation", "removal"})
    void deveDesserializarJsonBytesViaMessagingMessageListenerAdapter() throws Exception {
        MessageConverter converter = new Jackson2JsonMessageConverter();
        EmailMessagePayload payload = new EmailMessagePayload("adv@sgpj.com.br", "Assunto Teste", "<p>Corpo</p>");
        Message amqpMessage = converter.toMessage(payload, new MessageProperties());

        Method method = EmailQueueConsumer.class.getMethod("consumirEmail", EmailMessagePayload.class, Message.class);
        MessagingMessageListenerAdapter adapter = new MessagingMessageListenerAdapter(consumer, method);
        adapter.setMessageConverter(converter);

        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.afterPropertiesSet();
        adapter.setHandlerAdapter(new HandlerAdapter(factory.createInvocableHandlerMethod(consumer, method)));

        adapter.onMessage(amqpMessage, null);

        verify(emailService).buildEmail("adv@sgpj.com.br", "Assunto Teste", "<p>Corpo</p>");
    }
}
