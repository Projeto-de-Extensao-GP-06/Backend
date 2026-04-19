package br.com.sgpj.dto;

import java.util.List;

/**
 * Dados do usuário retornados no body da resposta de login.
 *
 * <p>Intencionalmente <b>não contém o token JWT</b> — o token é enviado
 *  como cookie HttpOnly via header {@code Set-Cookie}, inacessível ao JavaScript.
 *  Expô-lo no body seria contraditório com essa estratégia de segurança.</p>
 *
 * <p>O frontend usa esses dados apenas para fins de UX (exibir o nome,
 *  controlar rotas). A autenticação real continua sendo validada pelo
 *  cookie em cada requisição.</p>
 */
public record UsuarioSessaoDto(
        Long id,
        String nome,
        String email,
        CargoDto cargo,
        List<String> permissoes
) {
    /**
     * DTO simples para o cargo do usuário
     */
    public record CargoDto(
            Long id,
            String nome
    ) {}
}
