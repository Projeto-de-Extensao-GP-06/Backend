package br.com.sgpj.service.useCases.email;

public enum TemplateSetPassword {

    SET_PASSWORD_BODY("""
        <!DOCTYPE html>
        <html lang="pt-BR">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Boas-vindas ao SGPJ</title>
        </head>

        <body style="margin: 0; padding: 0; background-color: #f5f5f5;">

            <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">

                <div style="background: #0f2951; padding: 32px 24px;">
                    <h1 style="margin: 0; color: #ffffff;">
                        SGPJ
                    </h1>
                </div>

                <div style="padding: 40px 32px;">

                    <p>
                        Olá, <strong>%s</strong>!
                    </p>

                    <p style="color: #0f2951; font-weight: 600;">
                        🎉 Bem-vindo(a) ao SGPJ!
                    </p>

                    <p>
                        Sua conta foi criada com sucesso.
                    </p>

                    <p>
                        Para realizar o seu primeiro acesso e definir sua senha,
                        utilize o código de verificação abaixo:
                    </p>

                    <div style="
                        background-color: #0f2951;
                        border-radius: 8px;
                        padding: 24px;
                        margin: 32px 0;
                        text-align: center;
                    ">
                        <p style="color: #7e8ba3;">
                            CÓDIGO DE VERIFICAÇÃO
                        </p>

                        <p style="
                            color: #ffffff;
                            font-size: 36px;
                            font-weight: 700;
                            letter-spacing: 4px;
                        ">
                            %s
                        </p>
                    </div>

                    <div style="
                        background-color: #fff3e0;
                        padding: 16px;
                        margin: 24px 0;
                    ">
                        <p style="color: #e65100;">
                            <strong>⚠ Aviso:</strong>
                            Este código é válido por <strong>15 minutos</strong>,
                            permite até <strong>3 tentativas</strong> e só pode
                            ser utilizado uma vez com sucesso.
                        </p>
                    </div>

                    <p style="color: #d32f2f;">
                        Por questões de segurança, não compartilhe este código
                        com ninguém.
                    </p>

                    <p>
                        Atenciosamente,
                    </p>

                    <p style="color: #0f2951; font-weight: 600;">
                        Equipe SGPJ
                    </p>

                </div>

                <div style="
                    background-color: #f9f9f9;
                    padding: 24px 32px;
                    text-align: center;
                ">
                    <p style="color: #7e8ba3; font-size: 12px;">
                        Este é um e-mail automático.
                        Por favor, não responda a esta mensagem.
                    </p>

                    <p style="color: #9e9e9e; font-size: 11px;">
                        © 2026 SGPJ - Sistema de Gerenciamento de Processos Jurídicos
                    </p>
                </div>

            </div>

        </body>
        </html>
        """);

    private final String template;

    TemplateSetPassword(String template) {
        this.template = template;
    }

    public String format(String nomeUsuario, String otp) {
        return String.format(template, nomeUsuario, otp);
    }
}
