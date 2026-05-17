package br.com.sgpj.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EmailService")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private static final String EMAIL_DESTINATARIO = "usuario@teste.com";
    private static final String NOME_USUARIO = "João Silva";
    private static final String OTP_TESTE = "123456";
    private static final String REMETENTE = "noreply@sgpj.com";

    @Test
    @DisplayName("Deve enviar email de recuperação de senha com OTP")
    void deveEnviarEmailRecuperacaoSenhaComOtp() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);

        // When
        emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve enviar email com assunto correto para recuperação de senha")
    void deveEnviarEmailComAssuntoCorretoParaRecuperacaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("Código de verificação para recuperação de senha", message.getSubject());
    }

    @Test
    @DisplayName("Deve enviar email com corpo contendo nome do usuário na recuperação de senha")
    void deveEnviarEmailComCorpoContendoNomeUsuarioNaRecuperacaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains(NOME_USUARIO));
    }

    @Test
    @DisplayName("Deve enviar email com corpo contendo OTP na recuperação de senha")
    void deveEnviarEmailComCorpoContendoOtpNaRecuperacaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains(OTP_TESTE));
    }

    @Test
    @DisplayName("Deve enviar email com aviso de validade de 15 minutos na recuperação de senha")
    void deveEnviarEmailComAvisoValidadeNaRecuperacaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendForgotPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains("15 minutos"));
        assertTrue(message.getText().contains("3 tentativas"));
    }

    @Test
    @DisplayName("Deve enviar email de definição de senha com OTP")
    void deveEnviarEmailDefinicaoSenhaComOtp() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);

        // When
        emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve enviar email com assunto correto para definição de senha")
    void deveEnviarEmailComAssuntoCorretoParaDefinicaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("Código de verificação para definição de senha", message.getSubject());
    }

    @Test
    @DisplayName("Deve enviar email com corpo contendo nome do usuário na definição de senha")
    void deveEnviarEmailComCorpoContendoNomeUsuarioNaDefinicaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains(NOME_USUARIO));
    }

    @Test
    @DisplayName("Deve enviar email com corpo contendo OTP na definição de senha")
    void deveEnviarEmailComCorpoContendoOtpNaDefinicaoSenha() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendSetPasswordOtp(EMAIL_DESTINATARIO, NOME_USUARIO, OTP_TESTE);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getText().contains(OTP_TESTE));
    }

    @Test
    @DisplayName("Deve enviar email configurando destinatário corretamente")
    void deveEnviarEmailConfigurandoDestinatarioCorretamente() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto Teste", "Corpo do Email");

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertArrayEquals(new String[]{EMAIL_DESTINATARIO}, message.getTo());
    }

    @Test
    @DisplayName("Deve enviar email configurando remetente corretamente")
    void deveEnviarEmailConfigurandoRemetenteCorretamente() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // When
        emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto Teste", "Corpo do Email");

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals(REMETENTE, message.getFrom());
    }

    @Test
    @DisplayName("Deve enviar email configurando assunto corretamente")
    void deveEnviarEmailConfigurandoAssuntoCorretamente() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        String assunto = "Assunto de Teste";

        // When
        emailService.sendEmail(EMAIL_DESTINATARIO, assunto, "Corpo do Email");

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals(assunto, message.getSubject());
    }

    @Test
    @DisplayName("Deve enviar email configurando corpo corretamente")
    void deveEnviarEmailConfigurandoCorpoCorretamente() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        String corpo = "Este é o corpo do email de teste";

        // When
        emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", corpo);

        // Then
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals(corpo, message.getText());
    }

    @Test
    @DisplayName("Deve chamar mailSender exatamente uma vez ao enviar email")
    void deveChamarMailSenderExatamenteUmaVezAoEnviarEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "remetente", REMETENTE);

        // When
        emailService.sendEmail(EMAIL_DESTINATARIO, "Assunto", "Corpo");

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}