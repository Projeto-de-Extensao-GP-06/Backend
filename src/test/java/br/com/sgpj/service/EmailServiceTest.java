package br.com.sgpj.service;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import br.com.sgpj.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage mimeMessage;

    private static final String EMAIL_DESTINATARIO = "usuario@teste.com";
    private static final String NOME_USUARIO       = "João Silva";
    private static final String OTP_TESTE          = "123456";
    private static final String REMETENTE          = "noreply@sgpj.com";

    @BeforeEach
    void setUp() {
        // Injeta o remetente via @Value
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);

        // Retorna uma MimeMessage real para que o MimeMessageHelper consiga populá-la.
        // Session null é suficiente para testes unitários sem SMTP.
        mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // =========================================================================
    // Método sendEmail (baixo nível)
    // =========================================================================

    @Nested
    @DisplayName("sendEmail")
    class SendEmail {

        @Test
        @DisplayName("deve chamar mailSender.send exatamente uma vez")
        void deveChamarMailSenderUmaVez() {
            emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", "Corpo");

            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("deve configurar o destinatário corretamente")
        void deveConfigurarDestinatario() throws Exception {
            emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", "Corpo");

            String[] destinatarios = Arrays.stream(mimeMessage.getAllRecipients())
                    .map(Address::toString)
                    .toArray(String[]::new);
            assertArrayEquals(new String[]{EMAIL_DESTINATARIO}, destinatarios);
        }

        @Test
        @DisplayName("deve configurar o remetente corretamente")
        void deveConfigurarRemetente() throws Exception {
            emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", "Corpo");

            assertEquals(REMETENTE, mimeMessage.getFrom()[0].toString());
        }

        @Test
        @DisplayName("deve configurar o assunto corretamente")
        void deveConfigurarAssunto() throws Exception {
            String assunto = "Assunto de Teste";

            emailService.sendEmail(EMAIL_DESTINATARIO, assunto, "Corpo");

            assertEquals(assunto, mimeMessage.getSubject());
        }

        @Test
        @DisplayName("deve configurar o corpo corretamente")
        void deveConfigurarCorpo() throws Exception {
            String corpo = "<p>Este é o corpo do email de teste</p>";

            emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", corpo);

            assertTrue(extrairConteudo(mimeMessage).contains(corpo));
        }

        @Test
        @DisplayName("deve lançar EmailSendException quando MimeMessageHelper lança MessagingException")
        void deveLancarEmailSendExceptionQuandoOcorreMessagingException() throws Exception {
            // Força MessagingException no construtor do MimeMessageHelper:
            // com multipart=true, o helper chama mimeMessage.setContent(Multipart)
            // antes de qualquer outra operação — é o ponto mais confiável para injetar a falha.
            MimeMessage mensagemFalha = spy(new MimeMessage((Session) null));
            doThrow(new MessagingException("Erro de conexão simulado"))
                    .when(mensagemFalha).setContent(any(Multipart.class));
            when(mailSender.createMimeMessage()).thenReturn(mensagemFalha);

            EmailSendException excecao = assertThrows(EmailSendException.class,
                    () -> emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", "Corpo"));

            assertTrue(excecao.getMessage().startsWith("Erro ao enviar email. Detalhes:"));
            assertTrue(excecao.getMessage().contains("Erro de conexão simulado"));
        }
    }

    // =========================================================================
    // Método sendForgotPasswordOtp
    // =========================================================================

    @Nested
    @DisplayName("sendForgotPasswordOtp")
    class SendForgotPasswordOtp {

        @Test
        @DisplayName("deve chamar mailSender.send exatamente uma vez")
        void deveChamarMailSenderUmaVez() {
            emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("deve enviar com o assunto correto")
        void deveEnviarComAssuntoCorreto() throws Exception {
            emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertEquals("Código de verificação para recuperação de senha", mimeMessage.getSubject());
        }

        @Test
        @DisplayName("deve incluir o nome do usuário no corpo")
        void deveIncluirNomeUsuarioNoCorpo() throws Exception {
            emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertTrue(extrairConteudo(mimeMessage).contains(NOME_USUARIO));
        }

        @Test
        @DisplayName("deve incluir o OTP no corpo")
        void deveIncluirOtpNoCorpo() throws Exception {
            emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertTrue(extrairConteudo(mimeMessage).contains(OTP_TESTE));
        }

        @Test
        @DisplayName("deve incluir aviso de validade de 15 minutos no corpo")
        void deveIncluirAvisoValidadeNoCorpo() throws Exception {
            emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            String conteudo = extrairConteudo(mimeMessage);
            assertTrue(conteudo.contains("15 minutos"));
            assertTrue(conteudo.contains("3 tentativas"));
        }
    }

    // =========================================================================
    // Método sendSetPasswordOtp
    // =========================================================================

    @Nested
    @DisplayName("sendSetPasswordOtp")
    class SendSetPasswordOtp {

        @Test
        @DisplayName("deve chamar mailSender.send exatamente uma vez")
        void deveChamarMailSenderUmaVez() {
            emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            verify(mailSender, times(1)).send(mimeMessage);
        }

        @Test
        @DisplayName("deve enviar com o assunto correto")
        void deveEnviarComAssuntoCorreto() throws Exception {
            emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertEquals("Código de verificação para definição de senha", mimeMessage.getSubject());
        }

        @Test
        @DisplayName("deve incluir o nome do usuário no corpo")
        void deveIncluirNomeUsuarioNoCorpo() throws Exception {
            emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertTrue(extrairConteudo(mimeMessage).contains(NOME_USUARIO));
        }

        @Test
        @DisplayName("deve incluir o OTP no corpo")
        void deveIncluirOtpNoCorpo() throws Exception {
            emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

            assertTrue(extrairConteudo(mimeMessage).contains(OTP_TESTE));
        }
    }

    // =========================================================================
    // Utilitário — extrai todo o texto de um MimeMessage (suporte a multipart)
    // =========================================================================

    /**
     * Extrai recursivamente o conteúdo textual de um {@link MimeMessage},
     * suportando mensagens simples e {@code multipart/alternative} geradas
     * pelo {@link org.springframework.mail.javamail.MimeMessageHelper}.
     */
    private String extrairConteudo(MimeMessage message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String texto) {
            return texto;
        }
        if (content instanceof MimeMultipart multipart) {
            return extrairDeMultipart(multipart);
        }
        return "";
    }

    private String extrairDeMultipart(MimeMultipart multipart) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            Object content = part.getContent();
            if (content instanceof MimeMultipart nested) {
                sb.append(extrairDeMultipart(nested));
            } else {
                sb.append(content.toString());
            }
        }
        return sb.toString();
    }
}