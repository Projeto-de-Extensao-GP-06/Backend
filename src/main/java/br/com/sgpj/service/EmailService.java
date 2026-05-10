package br.com.sgpj.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendForgotPasswordOtp(String to, String nomeUsuario, String otp) {
        String subject = "Código de verificação para recuperação de senha";
        String body = createForgotPasswordBody(nomeUsuario, otp);
        sendEmail(to, subject, body);
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    private String createForgotPasswordBody(String nomeUsuario, String otp) {
        return String.format("""
                Olá %s,
                
                Para continuar o processo de redefinição de senha, por favor, utilize o código de verificação abaixo:
                
                %s
                Aviso: Este código é válido por 15 minutos e só pode ser utilizado uma vez.
                Por questões de segurança, não compartilhe este código com ninguém.
                
                Se você não solicitou a redefinição de senha, não se preocupe.
                Basta ignorar este e-mail e sua conta continuará segura.
                
                Atenciosamente,
                
                Equipe SGPJ
                """, nomeUsuario, otp);
    }
}
