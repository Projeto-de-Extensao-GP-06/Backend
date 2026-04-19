package br.com.sgpj.config.swagger;

import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class SwaggerCustomCssInjector implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Intercepta o bean nativo do Springdoc responsável por renderizar o HTML
        if (bean instanceof SwaggerIndexTransformer original) {

            // Retorna uma versão modificada desse bean
            return new SwaggerIndexTransformer() {
                @Override
                public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain) throws IOException {
                    // 1. Deixa o Springdoc gerar o HTML original com todas as variáveis JS corretas
                    Resource transformed = original.transform(request, resource, transformerChain);

                    // 2. Lê o HTML gerado
                    try (InputStream is = transformed.getInputStream()) {
                        String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                        // 3. Injeta a tag do seu CSS logo antes de fechar a tag <head>
                        html = html.replace("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"/swagger-custom.css\" /></head>");

                        // 4. Retorna o novo HTML para o navegador
                        return new TransformedResource(transformed, html.getBytes(StandardCharsets.UTF_8));
                    }
                }
            };
        }
        return bean; // Se não for o bean do Swagger, ignora
    }
}
