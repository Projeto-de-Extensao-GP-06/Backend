package br.com.sgpj.service.useCases.email;

import br.com.sgpj.entity.Processo.ProcessoApp;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class RenderEmailBody {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

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

    public EmailContent renderWeeklyHearings(
            String nomeUsuario,
            LocalDate inicioSemana,
            LocalDate fimSemana,
            List<ProcessoApp> processos
    ) {
        String subject = "Resumo semanal de audiências - SGPJ";
        String itensHtml = processos.stream()
                .map(p -> TemplateWeeklyHearings.formatItem(
                        p.getDataAudiencia() != null ? p.getDataAudiencia().format(DATE_TIME_FORMATTER) : "",
                        p.getCliente() != null ? p.getCliente().getNomeRazaoSocial() : null,
                        p.getProcessoApi() != null ? p.getProcessoApi().getNumeroCnj() : null
                ))
                .collect(Collectors.joining("\n"));

        String body = TemplateWeeklyHearings.WEEKLY_HEARINGS_BODY.format(
                nomeUsuario,
                processos.size(),
                inicioSemana.format(DATE_FORMATTER),
                fimSemana.format(DATE_FORMATTER),
                itensHtml
        );

        return new EmailContent(subject, body);
    }

    public EmailContent renderDailyHearings(
            String nomeUsuario,
            LocalDate dataReferencia,
            List<ProcessoApp> processos
    ) {
        String subject = "Lembrete de audiências do dia - SGPJ";
        String itensHtml = processos.stream()
                .map(p -> TemplateDailyHearings.formatItem(
                        p.getDataAudiencia() != null ? p.getDataAudiencia().format(DATE_TIME_FORMATTER) : "",
                        p.getCliente() != null ? p.getCliente().getNomeRazaoSocial() : null,
                        p.getProcessoApi() != null ? p.getProcessoApi().getNumeroCnj() : null
                ))
                .collect(Collectors.joining("\n"));

        String body = TemplateDailyHearings.DAILY_HEARINGS_BODY.format(
                nomeUsuario,
                processos.size(),
                dataReferencia.format(DATE_FORMATTER),
                itensHtml
        );

        return new EmailContent(subject, body);
    }

}
