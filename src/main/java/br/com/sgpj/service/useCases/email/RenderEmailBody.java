package br.com.sgpj.service.useCases.email;

import org.springframework.stereotype.Component;


@Component
public class RenderEmailBody {

    public record EmailContent(String subject, String body) {}

    public EmailContent renderForgotPasswordOtp(String nomeUsuario, String otp) {
        String subject = "Código de verificação para recuperação de senha";
        String body = TemplateForgotPassword.FORGOT_PASSWORD_BODY.format(nomeUsuario, otp);

        return new EmailContent(subject, body);
    }

    public EmailContent renderSetPasswordOtp(String nomeUsuario, String otp) {
        String subject = "Código de verificação para definição de senha";
        String body = TemplateSetPassword.SET_PASSWORD_BODY.format(nomeUsuario, otp);

        return new EmailContent(subject, body);
    }

}
