package br.com.sgpj.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import br.com.sgpj.dto.UsuarioCriacaoDto;
import br.com.sgpj.dto.UsuarioListarDto;
import br.com.sgpj.dto.UsuarioLoginDto;
import br.com.sgpj.dto.UsuarioSessaoDto;
import br.com.sgpj.dto.UsuarioTokenDto;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    public static final String COOKIE_NOME = "authToken";

    @Value("${jwt.validity}")
    private long jwtValidity;

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<Void> criar(@RequestBody @Valid UsuarioCriacaoDto usuarioCriacaoDto) {
        final Usuario novoUsuario = UsuarioMapper.toEntity(usuarioCriacaoDto);
        this.usuarioService.criar(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Autentica o usuário e retorna o token JWT como cookie HttpOnly.
     */
    @PostMapping("/login")
    public ResponseEntity<UsuarioSessaoDto> login(
            @RequestBody @Valid UsuarioLoginDto usuarioLoginDto,
            HttpServletResponse response) {

        final Usuario usuario = UsuarioMapper.toEntity(usuarioLoginDto);
        UsuarioTokenDto autenticado = usuarioService.autenticar(usuario);

        adicionarCookie(response, autenticado.token(), Duration.ofSeconds(jwtValidity));

        // Body retorna apenas dados de sessao - sem o token
        UsuarioSessaoDto sessao = UsuarioMapper.toSessaoDto(autenticado);
        return ResponseEntity.ok(sessao);
    }

    /**
     * Invalida a sessão do usuário limpando o cookie de autenticação.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        adicionarCookie(response, "", Duration.ZERO);   // expira o token JWT de imediato
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<List<UsuarioListarDto>> listarTodos() {
        List<UsuarioListarDto> usuariosEncontrados = usuarioService.listarTodos();

        if (usuariosEncontrados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuariosEncontrados);
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
    private void adicionarCookie(HttpServletResponse response, String valor, Duration duracao) {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NOME, valor)
                .httpOnly(true)             // inacessível ao JavaScript
                .secure(false)              // true em produção (exige HTTPS)
                .sameSite("Strict")         // bloqueia envio cross-site (mitiga CSRF)
                .path("/")                  // válido para toda a aplicação
                .maxAge(duracao)            // expira junto com o token JWT
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
