package br.com.sgpj.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthenticationCookieService {

    private final boolean cookieSecure;

    public AuthenticationCookieService(
            @Value("${cookie.secure}") boolean cookieSecure
    ) {
        this.cookieSecure = cookieSecure;
    }

    public void adicionar(
            HttpServletResponse response,
            String token,
            Duration duracao,
            String nomeCookie,
            String path
    ) {
        ResponseCookie cookie = ResponseCookie
                .from(nomeCookie, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path(path)
                .maxAge(duracao)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public void remover(HttpServletResponse response, String nomeCookie, String path) {
        adicionar(response, "", Duration.ZERO, nomeCookie, path);
    }
}