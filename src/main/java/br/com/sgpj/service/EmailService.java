package br.com.sgpj.service;

import br.com.sgpj.config.RabbitMQEmailConfig;
import br.com.sgpj.dto.EmailMessagePayload;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.EmailSendException;
import br.com.sgpj.service.useCases.email.RenderEmailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RenderEmailBody renderEmailBody;
    private final RabbitTemplate rabbitTemplate;
    private final String remetente;

    public EmailService(
            JavaMailSender mailSender,
            RenderEmailBody renderEmailBody,
            RabbitTemplate rabbitTemplate,
            @Value("${spring.mail.username.from}") String remetente
    ) {
        this.mailSender = mailSender;
        this.renderEmailBody = renderEmailBody;
        this.rabbitTemplate = rabbitTemplate;
        this.remetente = remetente;
    }

    public void sendForgotPasswordOtp(String to, String nomeUsuario, String otp) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderForgotPasswordOtp(nomeUsuario, otp);
        enqueueEmail(to, emailContent.subject(), emailContent.body());
    }

    public void sendSetPasswordOtp(String to, String nomeUsuario, String otp) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderSetPasswordOtp(nomeUsuario, otp);
        enqueueEmail(to, emailContent.subject(), emailContent.body());
    }

    public void sendWeeklyHearings(String to, String nomeUsuario, LocalDate inicioSemana, LocalDate fimSemana, List<ProcessoApp> processos) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderWeeklyHearings(nomeUsuario, inicioSemana, fimSemana, processos);
        enqueueEmail(to, emailContent.subject(), emailContent.body());
    }

    public void sendDailyHearings(String to, String nomeUsuario, LocalDate dataReferencia, List<ProcessoApp> processos) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderDailyHearings(nomeUsuario, dataReferencia, processos);
        enqueueEmail(to, emailContent.subject(), emailContent.body());
    }

    private void enqueueEmail(String to, String subject, String body) {
        EmailMessagePayload payload = new EmailMessagePayload(to, subject, body);
        rabbitTemplate.convertAndSend(
                RabbitMQEmailConfig.EMAIL_EXCHANGE,
                RabbitMQEmailConfig.ROUTING_KEY_SEND,
                payload
        );
    }

    public void buildEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = é HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendException("Erro ao enviar email. Detalhes: " + e.getMessage());
        }
    }

}
