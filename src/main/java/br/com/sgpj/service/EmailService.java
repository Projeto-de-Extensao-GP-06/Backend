package br.com.sgpj.service;

import br.com.sgpj.exception.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username.from}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendForgotPasswordOtp(String to, String nomeUsuario, String otp) {
        String subject = "Código de verificação para recuperação de senha";
        String body = createForgotPasswordBody(nomeUsuario, otp);
        sendEmail(to, subject, body);
    }

    public void sendSetPasswordOtp(String to, String nomeUsuario, String otp) {
        String subject = "Código de verificação para definição de senha";
        String body = createSetPasswordBody(nomeUsuario, otp);
        sendEmail(to, subject, body);
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
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

    private String createForgotPasswordBody(String nomeUsuario, String otp) {

        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Redefinição de Senha - SGPJ</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600;9..40,700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #0f2951 0%, #1a3a52 100%); padding: 32px 24px; text-align: left;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; letter-spacing: -0.5px; font-family: 'Playfair Display', serif;">SGPJ</h1>
                        </div>
                
                        <!-- Content -->
                        <div style="padding: 40px 32px;">
                            <!-- Greeting -->
                            <p style="margin: 0 0 24px 0; color: #1a1a1a; font-size: 16px; line-height: 1.6; font-family: 'DM Sans', sans-serif;">
                                Olá, <strong>""" +
                nomeUsuario +
                """
                            </strong>!
                        </p>
                        
                        <!-- Main Message -->
                        <p style="margin: 0 0 24px 0; color: #424242; font-size: 15px; line-height: 1.6;">
                            Para continuar o processo de redefinição de senha, por favor utilize o código de verificação abaixo:
                        </p>
                        
                        <!-- OTP Code Box -->
                        <div style="background-color: #0f2951; border-radius: 8px; padding: 24px; margin: 32px 0; text-align: center;">
                            <p style="margin: 0 0 12px 0; color: #7e8ba3; font-size: 13px; letter-spacing: 1px; text-transform: uppercase;">Código de Verificação</p>
                            <p style="margin: 0; color: #ffffff; font-size: 36px; font-weight: 700; letter-spacing: 4px; font-family: 'DM Sans', monospace;">""" +
                otp +
                """
                                        </p>
                                    </div>
                        
                                    <!-- Warning Box -->
                                    <div style="background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 16px; margin: 24px 0; border-radius: 4px;">
                                        <p style="margin: 0; color: #e65100; font-size: 13px; line-height: 1.6;">
                                            <strong>⚠ Aviso:</strong> Este código é válido por <strong>15 minutos</strong>, permite até <strong>3 tentativas</strong> de validação e só pode ser utilizado <strong>uma vez com sucesso</strong>.
                                        </p>
                                    </div>
                        
                                    <!-- Security Notice -->
                                    <p style="margin: 0 0 24px 0; color: #d32f2f; font-size: 14px; line-height: 1.6; font-weight: 500;">
                                        Por questões de segurança, não compartilhe este código com ninguém.
                                    </p>
                        
                                    <!-- Reassurance Message -->
                                    <div style="background-color: #f0f4f8; padding: 16px; border-radius: 4px; margin: 24px 0;">
                                        <p style="margin: 0; color: #424242; font-size: 14px; line-height: 1.6;">
                                            Se você não solicitou a redefinição de senha, não se preocupe. Basta ignorar este e-mail e sua conta continuará segura.
                                        </p>
                                    </div>
                        
                                    <!-- Signature -->
                                    <p style="margin: 32px 0 8px 0; color: #1a1a1a; font-size: 15px; line-height: 1.6;">
                                        Atenciosamente,
                                    </p>
                                    <p style="margin: 0 0 0 0; color: #0f2951; font-size: 16px; font-weight: 600; font-family: 'Playfair Display', serif;">
                                        Equipe SGPJ
                                    </p>
                                </div>
                        
                                <!-- Footer -->
                                <div style="background-color: #f9f9f9; border-top: 1px solid #e0e0e0; padding: 24px 32px; text-align: center;">
                                    <p style="margin: 0; color: #7e8ba3; font-size: 12px; line-height: 1.6;">
                                        Este é um e-mail automático. Por favor, não responda a esta mensagem.
                                    </p>
                                    <p style="margin: 12px 0 0 0; color: #9e9e9e; font-size: 11px; line-height: 1.5;">
                                        © 2026 SGPJ - Sistema de Gerenciamento de Processos Jurídicos
                                    </p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """;
    }

    private String createSetPasswordBody(String nomeUsuario, String otp) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Boas-vindas ao SGPJ</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600;9..40,700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
                </head>
                <body style="margin: 0; padding: 0; font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #f5f5f5;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #0f2951 0%, #1a3a52 100%); padding: 32px 24px; text-align: left;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; letter-spacing: -0.5px; font-family: 'Playfair Display', serif;">SGPJ</h1>
                        </div>
                
                        <!-- Content -->
                        <div style="padding: 40px 32px;">
                            <!-- Greeting -->
                            <p style="margin: 0 0 24px 0; color: #1a1a1a; font-size: 16px; line-height: 1.6; font-family: 'DM Sans', sans-serif;">
                                Olá, <strong>""" +
                nomeUsuario +
                """
                                </strong>!
                            </p>
                
                            <!-- Welcome Message -->
                            <p style="margin: 0 0 8px 0; color: #0f2951; font-size: 16px; font-weight: 600; line-height: 1.6;">
                                🎉 Bem-vindo(a) ao SGPJ!
                            </p>
                            <p style="margin: 0 0 24px 0; color: #424242; font-size: 15px; line-height: 1.6;">
                                Sua conta foi criada com sucesso.
                            </p>
                
                            <!-- Main Message -->
                            <p style="margin: 0 0 24px 0; color: #424242; font-size: 15px; line-height: 1.6;">
                                Para realizar o seu primeiro acesso e definir a sua senha, por favor utilize o código de verificação abaixo:
                            </p>
                
                            <!-- OTP Code Box -->
                            <div style="background-color: #0f2951; border-radius: 8px; padding: 24px; margin: 32px 0; text-align: center;">
                                <p style="margin: 0 0 12px 0; color: #7e8ba3; font-size: 13px; letter-spacing: 1px; text-transform: uppercase;">Código de Verificação</p>
                                <p style="margin: 0; color: #ffffff; font-size: 36px; font-weight: 700; letter-spacing: 4px; font-family: 'DM Sans', monospace;">""" +
                otp +
                """
                            </p>
                            </div>
                
                            <!-- Warning Box -->
                            <div style="background-color: #fff3e0; border-left: 4px solid #ff9800; padding: 16px; margin: 24px 0; border-radius: 4px;">
                                <p style="margin: 0; color: #e65100; font-size: 13px; line-height: 1.6;">
                                    <strong>⚠ Aviso:</strong> Este código é válido por <strong>15 minutos</strong>, permite até <strong>3 tentativas</strong> de validação e só pode ser utilizado <strong>uma vez com sucesso</strong>.
                                </p>
                            </div>
                
                            <!-- Security Notice -->
                            <p style="margin: 0 0 24px 0; color: #d32f2f; font-size: 14px; line-height: 1.6; font-weight: 500;">
                                Por questões de segurança, não compartilhe este código com ninguém.
                            </p>
                
                            <!-- Alert Message -->
                            <div style="background-color: #fce4ec; border-left: 4px solid #c2185b; padding: 16px; border-radius: 4px; margin: 24px 0;">
                                <p style="margin: 0; color: #880e4f; font-size: 14px; line-height: 1.6;">
                                    Se você desconhece este cadastro ou não estava aguardando este e-mail, por favor <strong>contate o administrador do sistema</strong>.
                                </p>
                            </div>
                
                            <!-- Signature -->
                            <p style="margin: 32px 0 8px 0; color: #1a1a1a; font-size: 15px; line-height: 1.6;">
                                Atenciosamente,
                            </p>
                            <p style="margin: 0 0 0 0; color: #0f2951; font-size: 16px; font-weight: 600; font-family: 'Playfair Display', serif;">
                                Equipe SGPJ
                            </p>
                        </div>
                
                        <!-- Footer -->
                        <div style="background-color: #f9f9f9; border-top: 1px solid #e0e0e0; padding: 24px 32px; text-align: center;">
                            <p style="margin: 0; color: #7e8ba3; font-size: 12px; line-height: 1.6;">
                                Este é um e-mail automático. Por favor, não responda a esta mensagem.
                            </p>
                            <p style="margin: 12px 0 0 0; color: #9e9e9e; font-size: 11px; line-height: 1.5;">
                                © 2026 SGPJ - Sistema de Gerenciamento de Processos Jurídicos
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
