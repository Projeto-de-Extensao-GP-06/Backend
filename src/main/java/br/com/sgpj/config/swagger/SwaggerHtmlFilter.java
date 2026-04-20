package br.com.sgpj.config.swagger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SwaggerHtmlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        // Intercepta apenas a chamada do HTML inicial do Swagger
        if (req.getRequestURI().contains("/swagger-ui/index.html")) {

            // "Envelopa" a resposta para conseguirmos ler o HTML que o Springdoc gerou
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

            // Deixa a requisição seguir seu fluxo normal
            chain.doFilter(request, wrapper);

            // Pega os bytes do HTML gerado
            byte[] responseArray = wrapper.getContentAsByteArray();
            String html = new String(responseArray, StandardCharsets.UTF_8);

            // Injeta a tag do SGPJ
            html = html.replace("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"/swagger-custom.css\" /></head>");

            // Escreve o HTML modificado de volta na saída real para o navegador
            byte[] modifiedResponse = html.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(modifiedResponse.length);
            response.getOutputStream().write(modifiedResponse);
            response.flushBuffer();

        } else {
            // Se for qualquer outra rota da API, segue normalmente sem peso extra
            chain.doFilter(request, response);
        }
    }
}