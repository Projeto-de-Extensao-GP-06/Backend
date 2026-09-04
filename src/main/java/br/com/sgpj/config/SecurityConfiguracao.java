package br.com.sgpj.config;

import br.com.sgpj.config.jwt.AutenticacaoEntryPoint;
import br.com.sgpj.config.jwt.AutenticacaoFilter;
import br.com.sgpj.config.jwt.AutenticacaoProvider;
import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import br.com.sgpj.exception.CustomAccessDeniedHandler;
import br.com.sgpj.security.TokenBlacklistService;
import br.com.sgpj.service.AutenticacaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração central do Spring Security para autenticação JWT stateless.
 *
 * <p>Esta classe define toda a política de segurança da aplicação:</p>
 * <ul>
 *   <li>Quais endpoints são públicos e quais exigem autenticação</li>
 *   <li>Como o JWT é processado em cada requisição (via filtro customizado)</li>
 *   <li>Política de sessão, CORS e CSRF</li>
 *   <li>Algoritmo de hash de senha</li>
 * </ul>
 *
 * <p><b>Anotações utilizadas:</b></p>
 * <ul>
 *   <li>{@code @Configuration}: indica que esta classe contém definições de beans Spring</li>
 *   <li>{@code @EnableWebSecurity}: ativa a configuração do Spring Security via código Java</li>
 *   <li>{@code @EnableMethodSecurity}: habilita anotações de segurança nos métodos
 *       (ex: {@code @PreAuthorize("hasRole('ADMIN')")})</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguracao {

    private final AutenticacaoService autenticacaoService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final TokenBlacklistService tokenBlacklistService;

    // AutenticacaoEntryPoint é registrado como @Component, o Spring injeta automaticamente
    private final AutenticacaoEntryPoint autenticacaoJwtEntryPoint;

    public SecurityConfiguracao(
            AutenticacaoService autenticacaoService,
            AutenticacaoEntryPoint autenticacaoJwtEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            TokenBlacklistService tokenBlacklistService) {
        this.autenticacaoService = autenticacaoService;
        this.autenticacaoJwtEntryPoint = autenticacaoJwtEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * URLs que não exigem autenticação (acesso público).
     *
     * <p>Inclui documentação da API (Swagger/OpenAPI), endpoints de login,
     * logout e rotas de erro — tudo que deve funcionar sem token JWT.</p>
     */
    private static final String[] URLS_PERMITIDAS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-custom.css",
            "/configuration/ui",
            "/configuration/security",
            "/api/public/**",
            "/api/public/authenticate",
            "/webjars/**",
            "/v3/api-docs/**",
            "/actuator/*",
            "/usuarios/login/**",
            "/usuarios/logout/**",
            "/usuarios/enviar-redefinir-senha-otp/**",
            "/usuarios/enviar-definir-senha-otp/**",
            "/usuarios/validar-otp/**",
            "/usuarios/refresh-token/**",
            "/api-onedrive/auth/callback",
            "/login/oauth2/code/azure",
            "/error/**"
    };

    /**
     * Define a cadeia de filtros de segurança (Security Filter Chain).
     *
     * <p>Este é o bean mais importante da configuração. Ele define como cada
     * requisição HTTP é processada pelo Spring Security.</p>
     *
     * @param http objeto para construir as configurações de segurança HTTP
     * @return cadeia de filtros configurada
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // Habilita CORS com a configuração definida em corsConfigurationSource()
                .cors(Customizer.withDefaults())

                // Desabilita CSRF (Cross-Site Request Forgery):
                // Embora esta API use cookies HttpOnly para armazenar o JWT,
                // a proteção CSRF é dispensável porque o cookie usa SameSite=Strict,
                // o que impede o envio automático em requisições cross-origin.
                .csrf(CsrfConfigurer<HttpSecurity>::disable)

                // Define quais URLs são públicas e quais exigem autenticação
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(URLS_PERMITIDAS).permitAll()  // rotas públicas
                        .requestMatchers(HttpMethod.POST, "/solicitacoes").permitAll()
                        .requestMatchers("/usuarios/definir-nova-senha").hasAuthority("SCOPE_SET_PASSWORD") // rota /definir-nova-senha exige claim set password
                        .anyRequest().hasAuthority("SCOPE_GENERAL_USE")  // todas as outras exigem token com claim general use
                )

                // Configura o handler para erros de autenticação (token ausente/inválido → 401/403)
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(autenticacaoJwtEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))

                // Define política de sessão: STATELESS
                // O servidor NÃO cria nem armazena sessões HTTP.
                // Cada requisição é autenticada de forma independente pelo token JWT.
                // Isso torna a API escalável horizontalmente (sem estado compartilhado entre servidores).
                .sessionManagement(management -> management
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Adiciona o filtro JWT ANTES do filtro padrão de autenticação por usuário/senha.
        // Isso garante que o token seja processado antes que o Spring Security tente
        // qualquer outro mecanismo de autenticação.
        http.addFilterBefore(autenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura o AuthenticationManager com o provider customizado.
     *
     * <p>O {@link AuthenticationManager} é o responsável por orquestrar a autenticação.
     * Aqui registramos nosso {@link AutenticacaoProvider}, que sabe validar credenciais
     * contra o banco de dados usando BCrypt.</p>
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(
                new AutenticacaoProvider(autenticacaoService, passwordEncoder()));
        return authenticationManagerBuilder.build();
    }

    /**
     * Cria o filtro de autenticação JWT.
     *
     * Este filtro intercepta todas as requisições e extrai/valida o token JWT
     * do header.
     */
    @Bean
    public AutenticacaoFilter autenticacaoFilter() {
        return new AutenticacaoFilter(autenticacaoService, gerenciadorTokenJwt(), tokenBlacklistService);
    }

    /**
     * Cria o gerenciador de tokens JWT.
     * Responsável por gerar, parsear e validar tokens JWT usando a biblioteca JJWT.
     */
    @Bean
    public GerenciadorTokenJwt gerenciadorTokenJwt() {
        return new GerenciadorTokenJwt();
    }

    /**
     * Define o algoritmo de hash para senhas: BCrypt.
     *
     * <p>BCrypt é um algoritmo de hash adaptativo que inclui um "salt" aleatório
     * automaticamente, tornando cada hash único mesmo para senhas idênticas.
     * O fator de custo padrão é 10, o que torna ataques de força bruta computacionalmente caros.</p>
     *
     * <p><b>Nunca armazene senhas em texto puro.</b> Sempre use um hash seguro como BCrypt.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura a política de CORS (Cross-Origin Resource Sharing).
     *
     * <p><b>Por que não usar {@code *} com cookies?</b><br>
     * Quando o frontend usa {@code withCredentials: true} (necessário para enviar cookies),
     * o navegador exige que o servidor informe uma origem específica em
     * {@code Access-Control-Allow-Origin}. O valor {@code *} é rejeitado pelo browser
     * nesse contexto — isso é uma regra do padrão CORS, não do Spring.</p>
     *
     * <p>Em desenvolvimento, o Vite roda em {@code http://localhost:5173} por padrão.
     * Em produção, substitua pela URL real do frontend.</p>
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();

        // Origens permitidas — deve ser explícita quando allowCredentials=true
        // Em produção: List.of("https://meuapp.com.br")
        configuracao.setAllowedOrigins(List.of(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000"   // Create React App (alternativa)
        ));

        // Necessário para que o browser envie/receba cookies nas requisições cross-origin
        configuracao.setAllowCredentials(true);

        configuracao.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));

        // Permite todos os headers de requisição (Content-Type, Authorization etc.)
        configuracao.setAllowedHeaders(List.of("*"));

        // Expõe o header Content-Disposition para download de arquivos
        configuracao.setExposedHeaders(List.of(HttpHeaders.CONTENT_DISPOSITION));

        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/**", configuracao);

        return origem;
    }
}
