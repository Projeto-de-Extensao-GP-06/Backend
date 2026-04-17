package br.com.sgpj.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SGPJ - Sistema de Gestão de Processos Jurídicos",
                description = "Solução web para centralização e gestão de processos jurídicos, " +
                        "criada como projeto de extensão acadêmico.",
                contact = @Contact(
                        name = "Github SGPJ",
                        url = "https://github.com/Projeto-de-Extensao-GP-06"
                ),
                version = "1.0.0"
        )
)
@SecurityScheme(
        name = "Bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT"
)
public class OpenApiConfig {

}
