package br.com.sgpj.consumer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.exception.DataJudIntegrationException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.service.SincronizacaoStatusService;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SincronizacaoProcessoConsumer")
class SincronizacaoProcessoConsumerTest {

    @Mock
    private UpdateProcesso updateProcesso;

    @Mock
    private SincronizacaoStatusService statusService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private SincronizacaoProcessoConsumer consumer;

    private static final int MAX_RETRIES = 3;

    @BeforeEach
    void setUp() {
        consumer = new SincronizacaoProcessoConsumer(updateProcesso, statusService, rabbitTemplate, MAX_RETRIES);
    }

    @Test
    @DisplayName("Deve processar sincronização com sucesso e notificar eventos")
    void deveProcessarSincronizacaoComSucesso() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(100L);
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        consumer.processarSincronizacao(mensagem, amqpMessage);

        verify(updateProcesso).atualizarDadosDoDataJud(100L);
        verify(statusService).notificar(eq(100L), eq("PROCESSANDO"), anyMap());
        verify(statusService).notificar(eq(100L), eq("CONCLUIDO"), argThat(map -> "SUCESSO".equals(map.get("status"))));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve enviar direto para DLQ e notificar erro em caso de ObjetoNaoEncontradoException (não retentável)")
    void deveEnviarDiretoParaDlqEmErroDeObjetoNaoEncontrado() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(999L);
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        doThrow(new ObjetoNaoEncontradoException("Processo não encontrado"))
                .when(updateProcesso).atualizarDadosDoDataJud(999L);

        consumer.processarSincronizacao(mensagem, amqpMessage);

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.DLX, RabbitMQConfig.ROUTING_KEY_DLQ, mensagem);
        verify(statusService).notificar(eq(999L), eq("CONCLUIDO"), argThat(map -> "ERRO".equals(map.get("status"))));
    }

    @Test
    @DisplayName("Deve enviar direto para DLQ e notificar erro em caso de OperacaoInvalidaException (não retentável)")
    void deveEnviarDiretoParaDlqEmErroDeOperacaoInvalida() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(50L);
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        doThrow(new OperacaoInvalidaException("Processo sem CNJ vinculado"))
                .when(updateProcesso).atualizarDadosDoDataJud(50L);

        consumer.processarSincronizacao(mensagem, amqpMessage);

        verify(rabbitTemplate).convertAndSend(RabbitMQConfig.DLX, RabbitMQConfig.ROUTING_KEY_DLQ, mensagem);
        verify(statusService).notificar(eq(50L), eq("CONCLUIDO"), argThat(map -> "ERRO".equals(map.get("status"))));
    }

    @Test
    @DisplayName("Deve rejeitar mensagem sem requeue (DLX/TTL) na primeira falha transitória sem x-death")
    void deveRejeitarParaDlxNaPrimeiraFalhaTransitoria() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(10L);
        Message amqpMessage = new Message(new byte[0], new MessageProperties());

        doThrow(new DataJudIntegrationException("Timeout na conexão"))
                .when(updateProcesso).atualizarDadosDoDataJud(10L);

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.processarSincronizacao(mensagem, amqpMessage));

        verify(statusService).notificar(eq(10L), eq("REPROCESSANDO"), anyMap());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve rejeitar mensagem sem requeue quando x-death count for menor que maxRetries")
    void deveRejeitarParaDlxQuandoTentativasMenoresQueLimite() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(10L);
        MessageProperties props = new MessageProperties();
        props.setHeader("x-death", List.of(Map.of("queue", RabbitMQConfig.QUEUE, "count", 2L)));
        Message amqpMessage = new Message(new byte[0], props);

        doThrow(new DataJudIntegrationException("DataJud indisponível"))
                .when(updateProcesso).atualizarDadosDoDataJud(10L);

        assertThrows(AmqpRejectAndDontRequeueException.class,
                () -> consumer.processarSincronizacao(mensagem, amqpMessage));

        verify(statusService).notificar(eq(10L), eq("REPROCESSANDO"), anyMap());
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("Deve encaminhar mensagem para DLQ final quando x-death count atingir maxRetries")
    void deveEncaminharParaDlqFinalQuandoAtingirLimite() {
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(10L);
        MessageProperties props = new MessageProperties();
        props.setHeader("x-death", List.of(Map.of("queue", RabbitMQConfig.QUEUE, "count", 3L)));
        Message amqpMessage = new Message(new byte[0], props);

        doThrow(new DataJudIntegrationException("Falha permanente de conexão"))
                .when(updateProcesso).atualizarDadosDoDataJud(10L);

        assertDoesNotThrow(() -> consumer.processarSincronizacao(mensagem, amqpMessage));

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.DLX,
                RabbitMQConfig.ROUTING_KEY_DLQ,
                mensagem
        );
        verify(statusService).notificar(eq(10L), eq("CONCLUIDO"), argThat(map -> "ERRO".equals(map.get("status"))));
    }

    @Test
    @DisplayName("Deve desserializar JSON para SincronizacaoProcessoMessage via MessagingMessageListenerAdapter")
    @SuppressWarnings({"deprecation", "removal"})
    void deveDesserializarJsonBytesViaMessagingMessageListenerAdapter() throws Exception {
        MessageConverter converter = new Jackson2JsonMessageConverter();
        SincronizacaoProcessoMessage mensagem = new SincronizacaoProcessoMessage(42L);
        Message amqpMessage = converter.toMessage(mensagem, new MessageProperties());

        Method method = SincronizacaoProcessoConsumer.class.getMethod("processarSincronizacao", SincronizacaoProcessoMessage.class, Message.class);
        MessagingMessageListenerAdapter adapter = new MessagingMessageListenerAdapter(consumer, method);
        adapter.setMessageConverter(converter);

        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.afterPropertiesSet();
        adapter.setHandlerAdapter(new HandlerAdapter(factory.createInvocableHandlerMethod(consumer, method)));

        adapter.onMessage(amqpMessage, null);

        verify(updateProcesso).atualizarDadosDoDataJud(42L);
    }
}
