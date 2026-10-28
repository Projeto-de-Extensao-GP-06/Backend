package br.com.sgpj.service.useCases.email;

public enum TemplateWeeklyHearings {

    WEEKLY_HEARINGS_BODY("""
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Audiências da Semana - SGPJ</title>
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=DM+Sans:opsz,wght@9..40,400;9..40,500;9..40,600;9..40,700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
        </head>
        <body style="margin: 0; padding: 0; font-family: 'DM Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background-color: #f5f5f5;">
            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                <!-- Header -->
                <div style="background: linear-gradient(135deg, #0f2951 0%%, #1a3a52 100%%); padding: 32px 24px; text-align: left;">
                    <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600; letter-spacing: -0.5px; font-family: 'Playfair Display', serif;">SGPJ</h1>
                </div>

                <!-- Content -->
                <div style="padding: 40px 32px;">
                    <!-- Greeting -->
                    <p style="margin: 0 0 24px 0; color: #1a1a1a; font-size: 16px; line-height: 1.6; font-family: 'DM Sans', sans-serif;">
                        Olá, <strong>%s</strong>!
                    </p>

                    <p style="margin: 0 0 16px 0; color: #0f2951; font-size: 16px; font-weight: 600;">
                        📅 Resumo Semanal de Audiências
                    </p>

                    <!-- Main Message -->
                    <p style="margin: 0 0 24px 0; color: #424242; font-size: 15px; line-height: 1.6;">
                        Você possui <strong>%d audiência(s)</strong> marcada(s) para esta semana (<strong>%s a %s</strong>). Confira abaixo a programação:
                    </p>

                    <!-- Hearings List -->
                    <div style="margin: 28px 0;">
                        %s
                    </div>

                    <!-- Info Box -->
                    <div style="background-color: #f0f4f8; border-left: 4px solid #0f2951; padding: 16px; border-radius: 4px; margin: 24px 0;">
                        <p style="margin: 0; color: #424242; font-size: 14px; line-height: 1.6;">
                            Acesse o sistema <strong>SGPJ</strong> para consultar os detalhes completos e a documentação de cada processo.
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
        </html>""");

    private static final String ITEM_TEMPLATE = """
        <div style="background-color: #f8fafc; border: 1px solid #e2e8f0; border-left: 4px solid #0f2951; border-radius: 6px; padding: 16px 20px; margin-bottom: 16px;">
            <p style="margin: 0 0 8px 0; color: #0f2951; font-size: 15px; font-weight: 700;">
                🕒 %s
            </p>
            <p style="margin: 0 0 4px 0; color: #424242; font-size: 14px; line-height: 1.5;">
                <strong>Cliente:</strong> %s
            </p>
            <p style="margin: 0; color: #424242; font-size: 14px; line-height: 1.5;">
                <strong>Número CNJ:</strong> %s
            </p>
        </div>""";

    private final String template;

    TemplateWeeklyHearings(String template) {
        this.template = template;
    }

    public String format(String nomeUsuario, int quantidadeAudiencias, String dataInicio, String dataFim, String itensHtml) {
        return String.format(
                template,
                escapeHtml(nomeUsuario != null ? nomeUsuario : ""),
                quantidadeAudiencias,
                escapeHtml(dataInicio != null ? dataInicio : ""),
                escapeHtml(dataFim != null ? dataFim : ""),
                itensHtml != null ? itensHtml : ""
        );
    }

    public static String formatItem(String dataHora, String nomeCliente, String numeroCnj) {
        return String.format(
                ITEM_TEMPLATE,
                escapeHtml(dataHora != null ? dataHora : ""),
                escapeHtml(nomeCliente != null ? nomeCliente : "Não informado"),
                escapeHtml(numeroCnj != null && !numeroCnj.isBlank() ? numeroCnj : "Não informado")
        );
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
