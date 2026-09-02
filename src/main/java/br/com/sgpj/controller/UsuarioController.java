package br.com.sgpj.controller;

import br.com.sgpj.dto.request.*;
import br.com.sgpj.dto.response.RefreshTokenResponse;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.dto.response.UsuarioSessaoResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
public class UsuarioController {

    public static final String ACCESS_TOKEN_COOKIE = "authToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    @Value("${jwt.validity}")
    private long jwtValidity;

    @Value("${jwt.refresh.validity}")
    private long refreshValidity;

    private final UsuarioService usuarioService;
    private final RefreshTokenService refreshTokenService;

    public UsuarioController(UsuarioService usuarioService, RefreshTokenService refreshTokenService) {
        this.usuarioService = usuarioService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('CRIAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')")
    public ResponseEntity<Void> criar(@RequestBody @Valid UsuarioCriacaoRequest usuarioCriacaoRequest) {
        this.usuarioService.criar(usuarioCriacaoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Autentica o usuário e retorna o token JWT como cookie HttpOnly.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioSessaoResponse> login(
            @RequestBody @Valid UsuarioLoginRequest usuarioLoginRequestDto,
            HttpServletResponse response) {

        final Usuario usuario = UsuarioMapper.toEntity(usuarioLoginRequestDto);
        UsuarioTokenResponse autenticado = usuarioService.autenticar(usuario);

        adicionarCookie(response, ACCESS_TOKEN_COOKIE, autenticado.token(), "/", Duration.ofSeconds(jwtValidity));
        adicionarCookie(response, REFRESH_TOKEN_COOKIE, autenticado.refreshToken(), "/usuarios/refresh-token", Duration.ofSeconds(refreshValidity));

        // Busca o usuário novamente para obter cargo e permissões
        Usuario usuarioComPermissoes = usuarioService.obterPorId(autenticado.id());
        
        // Body retorna dados de sessão com cargo e permissões
        UsuarioSessaoResponse sessao = UsuarioMapper.toSessaoDto(autenticado, usuarioComPermissoes);
        return ResponseEntity.ok(sessao);
    }

    /**
     * Invalida a sessão do usuário limpando o cookie de autenticação.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revogarToken(refreshToken);
        }
        adicionarCookie(response, ACCESS_TOKEN_COOKIE, "", "", Duration.ZERO);   // expira o token JWT de imediato
        adicionarCookie(response, REFRESH_TOKEN_COOKIE, "", "", Duration.ZERO);   // expira o token de refresh de imediato
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<UsuarioListarResponse>> listarTodos() {
        List<UsuarioListarResponse> usuariosEncontrados = usuarioService.listarTodos();

        if (usuariosEncontrados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuariosEncontrados);
    }

    @GetMapping("/funcao")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<UsuarioListarResponse>> listarPorFuncao(@RequestParam String funcao) {
        List<UsuarioListarResponse> usuariosEncontrados = usuarioService.listarPorFuncao(funcao);

        if (usuariosEncontrados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuariosEncontrados);
    }

    @GetMapping("/pesquisar")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<UsuarioListarResponse>> pesquisarPorNomeOuEmail(@RequestParam String pesquisa) {
        List<UsuarioListarResponse> usuariosEncontrados = usuarioService.buscarPorNomeOuEmail(pesquisa);

        if (usuariosEncontrados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuariosEncontrados);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('EDITAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')")
    public ResponseEntity<UsuarioListarResponse> atualizarUsuario(@Valid @RequestBody UsuarioAtualizacaoRequest atualizacao,
                                                                  @PathVariable Long id){
        UsuarioListarResponse usuarioEditado = usuarioService.atualizarUsuario(atualizacao, id);
        return ResponseEntity.status(HttpStatus.OK).body(usuarioEditado);
    }

    /**
     * Cria e adiciona o cookie de autenticação na resposta HTTP.
     *
     * <p><b>HttpOnly:</b> inacessível ao JavaScript (proteção XSS).</p>
     * <p><b>SameSite=Strict:</b> bloqueia envio cross-site (mitiga CSRF).</p>
     * <p><b>Secure:</b> {@code true} em produção (exige HTTPS).</p>
     *
     * @param response resposta HTTP onde o cookie será adicionado
     * @param valor    valor do cookie (token JWT ou vazio para logout)
     * @param duracao  tempo de vida do cookie ({@code Duration.ZERO} para deletar)
     */
    private void adicionarCookie(HttpServletResponse response, String nome, String valor, String path, Duration duracao) {
        ResponseCookie cookie = ResponseCookie.from(nome, valor)
                .httpOnly(true)             // inacessível ao JavaScript
                .secure(false)              // true em produção (exige HTTPS)
                .sameSite("Strict")         // bloqueia envio cross-site (mitiga CSRF)
                .path(path)                  // válido para toda a aplicação
                .maxAge(duracao)            // expira junto com o token JWT
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer")
    @PreAuthorize("hasAuthority('EXCLUIR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!usuarioService.isDifferentId(id)) {
            throw new UsuarioAutoExclusaoException("Não é possível excluir o próprio usuário.");
        }

        this.usuarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/enviar-redefinir-senha-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> enviarRedefinirSenhaOtp(@RequestBody EnviarOtpRequest request) {
        usuarioService.enviarRedefinirSenhaOtp(request.email());
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, um código será enviado em instantes.");
    }

    @PostMapping("/enviar-definir-senha-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> enviarDefinirSenhaOtp(@RequestBody EnviarOtpRequest request) {
        usuarioService.enviarDefinirSenhaOtp(request.email());
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, um código será enviado em instantes.");
    }

    @PostMapping("/validar-otp")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> validateOtp(@RequestBody ValidarOtpRequest request,
                                              HttpServletResponse response) {
        if (usuarioService.validarOtp(request)) {
            UsuarioTokenResponse autenticado = usuarioService.autenticarOtp(request);
            adicionarCookie(response, ACCESS_TOKEN_COOKIE, autenticado.token(), "/", Duration.ofSeconds(900));

            return ResponseEntity.ok("Código validado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Código inválido ou expirado.");
        }
    }

    @PostMapping("/definir-nova-senha")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<String> definirNovaSenha(@RequestBody DefinirNovaSenhaRequest request,
                                                   HttpServletResponse response) {
        usuarioService.definirNovaSenha(request.senha());
        adicionarCookie(response, ACCESS_TOKEN_COOKIE, "", "", Duration.ZERO);
        return ResponseEntity.ok("Nova senha definida com sucesso!");
    }

    @PostMapping("/reactivate/{id}")
    @PreAuthorize("hasAuthority('EDITAR USUÁRIO') || hasAuthority('TODAS AS PERMISSÕES')")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<UsuarioListarResponse> reativarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok().body(usuarioService.reativarUsuario(id));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refreshToken(@CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
                                             HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshTokenResponse novosTokens = refreshTokenService.validarERotacionar(refreshToken);

        // Atualiza os dois cookies HttpOnly na resposta
        adicionarCookie(response, ACCESS_TOKEN_COOKIE, novosTokens.accessToken(), "/", Duration.ofSeconds(jwtValidity));
        adicionarCookie(response, REFRESH_TOKEN_COOKIE, novosTokens.refreshToken(), "/usuarios/refresh-token", Duration.ofSeconds(refreshValidity));

        return ResponseEntity.ok().build();
    }
}
