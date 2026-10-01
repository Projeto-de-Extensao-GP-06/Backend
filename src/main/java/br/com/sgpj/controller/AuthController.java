package br.com.sgpj.controller;

import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.EnviarOtpRequest;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.RefreshTokenResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioSessaoResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;

import br.com.sgpj.security.AuthenticationCookieService;
import br.com.sgpj.security.IpAddressUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.security.TokenBlacklistService;
import br.com.sgpj.config.jwt.GerenciadorTokenJwt;

import br.com.sgpj.service.RefreshTokenService;
import br.com.sgpj.service.UsuarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "authToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    @Value("${jwt.validity:3600}")
    private long jwtValidity;

    @Value("${jwt.refresh.validity:604800}")
    private long jwtRefreshValidity;

    private final UsuarioService usuarioService;

    private final RateLimiter rateLimiter;
    private final AuthenticationCookieService cookieService;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<UsuarioSessaoResponse> login(
            @RequestBody @Valid UsuarioLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {

        String identifier =
                IpAddressUtil.createRateLimitIdentifier(
                        httpRequest,
                        request.email()
                );

        rateLimiter.checkLoginRateLimit(identifier);

        UsuarioTokenResponse autenticado =
                usuarioService.autenticar(request);

        // Adiciona access token
        cookieService.adicionar(
                response,
                autenticado.token(),
                Duration.ofSeconds(jwtValidity),
                ACCESS_TOKEN_COOKIE,
                "/"
        );

        // Adiciona refresh token
        cookieService.adicionar(
                response,
                autenticado.refreshToken(),
                Duration.ofSeconds(jwtRefreshValidity),
                REFRESH_TOKEN_COOKIE,
                "/auth"
        );

        return ResponseEntity.ok(
                autenticado.sessao()
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String token =
                gerenciadorTokenJwt.extrairTokenDaRequisicao(request);

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revogarToken(refreshToken);
        }

        cookieService.remover(response, ACCESS_TOKEN_COOKIE, "/");
        cookieService.remover(response, REFRESH_TOKEN_COOKIE, "/auth");

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh-token")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> refreshToken(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
                                             HttpServletResponse response,
                                             HttpServletRequest request) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = gerenciadorTokenJwt.extrairTokenDaRequisicao(request);

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        RefreshTokenResponse novosTokens = refreshTokenService.validarERotacionar(refreshToken);

        cookieService.adicionar(
                response,
                novosTokens.accessToken(),
                Duration.ofSeconds(jwtValidity),
                ACCESS_TOKEN_COOKIE,
                "/"
        );

        cookieService.adicionar(
                response,
                novosTokens.refreshToken(),
                Duration.ofSeconds(jwtRefreshValidity),
                REFRESH_TOKEN_COOKIE,
                "/auth"
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/enviar-redefinir-senha-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> enviarRedefinirSenhaOtp(
            @RequestBody @Valid EnviarOtpRequest request,
            HttpServletRequest httpRequest
    ) {

        String identifier =
                IpAddressUtil.createRateLimitIdentifier(
                        httpRequest,
                        request.email()
                );

        rateLimiter.checkOtpRateLimit(identifier);

        usuarioService.enviarRedefinirSenhaOtp(
                request.email()
        );

        return ResponseEntity.ok(
                "Se o e-mail estiver cadastrado, um código será enviado em instantes."
        );
    }

    @PostMapping("/enviar-definir-senha-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> enviarDefinirSenhaOtp(
            @RequestBody @Valid EnviarOtpRequest request,
            HttpServletRequest httpRequest
    ) {

        String identifier =
                IpAddressUtil.createRateLimitIdentifier(
                        httpRequest,
                        request.email()
                );

        rateLimiter.checkOtpRateLimit(identifier);

        usuarioService.enviarDefinirSenhaOtp(
                request.email()
        );

        return ResponseEntity.ok(
                "Se o e-mail estiver cadastrado, um código será enviado em instantes."
        );
    }

    @PostMapping("/validar-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> validarOtp(
            @RequestBody @Valid ValidarOtpRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {

        String identifier =
                IpAddressUtil.createRateLimitIdentifier(
                        httpRequest,
                        request.email()
                );

        rateLimiter.checkOtpRateLimit(identifier);

        usuarioService.validarOtp(request);

        UsuarioTokenResponse autenticado =
                usuarioService.autenticarOtp(request);

        cookieService.adicionar(
                response,
                autenticado.token(),
                Duration.ofMinutes(15),
                ACCESS_TOKEN_COOKIE,
                "/auth"
        );

        return ResponseEntity.ok(
                "Código validado com sucesso!"
        );
    }

    @PostMapping("/definir-nova-senha")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<String> definirNovaSenha(
            @RequestBody @Valid DefinirNovaSenhaRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {

        usuarioService.definirNovaSenha(request);

        String token =
                gerenciadorTokenJwt.extrairTokenDaRequisicao(httpRequest);

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        cookieService.remover(response, ACCESS_TOKEN_COOKIE, "/auth");

        return ResponseEntity.ok(
                "Nova senha definida com sucesso!"
        );
    }
}