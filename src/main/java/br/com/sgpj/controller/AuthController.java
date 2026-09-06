package br.com.sgpj.controller;

import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.EnviarOtpRequest;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.UsuarioSessaoResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;

import br.com.sgpj.security.AuthenticationCookieService;
import br.com.sgpj.security.IpAddressUtil;
import br.com.sgpj.security.RateLimiter;
import br.com.sgpj.security.TokenBlacklistService;
import br.com.sgpj.config.jwt.GerenciadorTokenJwt;

import br.com.sgpj.service.UsuarioService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Autenticação")
public class AuthController {

    @Value("${jwt.validity}")
    private long jwtValidity;

    private final UsuarioService usuarioService;

    private final RateLimiter rateLimiter;
    private final AuthenticationCookieService cookieService;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(
            UsuarioService usuarioService,
            RateLimiter rateLimiter,
            AuthenticationCookieService cookieService,
            GerenciadorTokenJwt gerenciadorTokenJwt,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.usuarioService = usuarioService;
        this.rateLimiter = rateLimiter;
        this.cookieService = cookieService;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.tokenBlacklistService = tokenBlacklistService;
    }

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

        cookieService.adicionar(
                response,
                autenticado.token(),
                Duration.ofSeconds(jwtValidity)
        );

        return ResponseEntity.ok(
                autenticado.sessao()
        );
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String token =
                gerenciadorTokenJwt.extrairTokenDaRequisicao(request);

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        cookieService.remover(response);

        return ResponseEntity.noContent().build();
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

        UsuarioTokenResponse autenticado =
                usuarioService.autenticarOtp(request);

        cookieService.adicionar(
                response,
                autenticado.token(),
                Duration.ofMinutes(15)
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

        cookieService.remover(response);

        return ResponseEntity.ok(
                "Nova senha definida com sucesso!"
        );
    }
}