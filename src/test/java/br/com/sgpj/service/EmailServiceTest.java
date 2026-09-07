package br.com.sgpj.service;

import br.com.sgpj.exception.EmailSendException;
import br.com.sgpj.service.useCases.email.RenderEmailBody;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RenderEmailBody renderEmailBody;

    private EmailService emailService;

    private static final String REMETENTE = "nao-responda@sgpj.com.br";

    @BeforeEach
    void setUp() {
        // remetente agora é injetado direto no construtor — sem precisar de reflexão
        emailService = new EmailService(mailSender, renderEmailBody, REMETENTE);

        jakarta.mail.Session session = jakarta.mail.Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }
    @Nested
    @DisplayName("sendForgotPasswordOtp")
    class SendForgotPasswordOtpTest {

        @Test
        @DisplayName("Deve renderizar o template correto e enviar o email")
        void deveRenderizarEEnviarEmailDeRecuperacaoDeSenha() {
            RenderEmailBody.EmailContent conteudo = new RenderEmailBody.EmailContent(
                    "Código de verificação para recuperação de senha", "<p>corpo</p>");
            when(renderEmailBody.renderForgotPasswordOtp("João", "123456")).thenReturn(conteudo);

            emailService.sendForgotPasswordOtp("joao@sgpj.com.br", "João", "123456");

            verify(renderEmailBody).renderForgotPasswordOtp("João", "123456");
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("sendSetPasswordOtp")
    class SendSetPasswordOtpTest {

        @Test
        @DisplayName("Deve renderizar o template correto e enviar o email")
        void deveRenderizarEEnviarEmailDeDefinicaoDeSenha() {
            RenderEmailBody.EmailContent conteudo = new RenderEmailBody.EmailContent(
                    "Código de verificação para definição de senha", "<p>corpo</p>");
            when(renderEmailBody.renderSetPasswordOtp("Maria", "654321")).thenReturn(conteudo);

            emailService.sendSetPasswordOtp("maria@sgpj.com.br", "Maria", "654321");

            verify(renderEmailBody).renderSetPasswordOtp("Maria", "654321");
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("buildEmail")
    class BuildEmailTest {

        @Test
        @DisplayName("Deve montar a mensagem com remetente, destinatário, assunto e corpo HTML corretos")
        void deveMontarMensagemComDadosCorretos() throws Exception {
            emailService.buildEmail("destino@sgpj.com.br", "Assunto de teste", "<p>Olá</p>");

            org.mockito.ArgumentCaptor<MimeMessage> captor =
                    org.mockito.ArgumentCaptor.forClass(MimeMessage.class);
            verify(mailSender).send(captor.capture());

            MimeMessage enviado = captor.getValue();
            assertEquals("Assunto de teste", enviado.getSubject());
            assertEquals(REMETENTE, ((jakarta.mail.internet.InternetAddress) enviado.getFrom()[0]).getAddress());
            assertEquals("destino@sgpj.com.br",
                    ((jakarta.mail.internet.InternetAddress) enviado.getAllRecipients()[0]).getAddress());
            assertTrue(enviado.getContentType().contains("text"));
        }

        @Test
        @DisplayName("Deve lançar EmailSendException quando ocorrer uma MessagingException ao montar o email")
        void deveLancarEmailSendExceptionEmFalhaDeEnvio() {
            // Um endereço contendo quebra de linha é rejeitado pelo parser de endereços do Jakarta Mail,
            // simulando uma falha real de MessagingException (e também uma tentativa de header injection).
            String destinatarioInvalido = "invalido\ninjection@sgpj.com.br";

            EmailSendException ex = assertThrows(EmailSendException.class,
                    () -> emailService.buildEmail(destinatarioInvalido, "Assunto", "<p>corpo</p>"));

            assertTrue(ex.getMessage().contains("Erro ao enviar email"));
            verify(mailSender, never()).send(any(MimeMessage.class));
        }
    }
}
