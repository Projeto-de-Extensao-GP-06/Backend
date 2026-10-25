package br.com.sgpj.service;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.EmailSendException;
import br.com.sgpj.service.useCases.email.RenderEmailBody;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RenderEmailBody renderEmailBody;
    private final String remetente;



    public EmailService(JavaMailSender mailSender, RenderEmailBody renderEmailBody, @Value("${spring.mail.username.from}")
    String remetente) {
        this.mailSender = mailSender;
        this.renderEmailBody = renderEmailBody;
        this.remetente = remetente;
    }

    @Async
    public void sendForgotPasswordOtp(String to, String nomeUsuario, String otp) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderForgotPasswordOtp(nomeUsuario, otp);
        String subject = emailContent.subject();
        String body = emailContent.body();

        buildEmail(to, subject, body);
    }

    @Async
    public void sendSetPasswordOtp(String to, String nomeUsuario, String otp) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderSetPasswordOtp(nomeUsuario, otp);
        String subject = emailContent.subject();
        String body = emailContent.body();

        buildEmail(to, subject, body);
    }

    @Async
    public void sendWeeklyHearings(String to, String nomeUsuario, LocalDate inicioSemana, LocalDate fimSemana, List<ProcessoApp> processos) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderWeeklyHearings(nomeUsuario, inicioSemana, fimSemana, processos);
        String subject = emailContent.subject();
        String body = emailContent.body();

        buildEmail(to, subject, body);
    }

    @Async
    public void sendDailyHearings(String to, String nomeUsuario, LocalDate dataReferencia, List<ProcessoApp> processos) {
        RenderEmailBody.EmailContent emailContent = renderEmailBody.renderDailyHearings(nomeUsuario, dataReferencia, processos);
        String subject = emailContent.subject();
        String body = emailContent.body();

        buildEmail(to, subject, body);
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
