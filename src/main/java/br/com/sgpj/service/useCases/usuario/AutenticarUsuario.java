package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioSessaoResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final RefreshTokenService refreshTokenService;

    public AutenticacaoUsuarioResponse executar(UsuarioLoginRequest request) {

        Usuario usuario =
                usuarioRepository.findByEmail(request.email());

        if (usuario == null) {
            log.error("Falha na autenticação de usuário. Motivo: usuário {} não encontrado.", request.email());
            throw new BadCredentialsException("E-mail e/ou senha inválido(s).");
        }

        if (!usuario.isAtivo() || usuario.isUsuarioSistema()) {
            log.error("Falha na autenticação de usuário. Motivo: usuário {} inativo ou de uso exclusivo do sistema.", request.email());
            throw new BadCredentialsException("E-mail e/ou senha inválido(s).");
        }

        log.info("Autenticando usuário {} para uso normal.", request.email());

        Authentication authentication =
                usuarioAutenticacaoAdapter.autenticar(
                        request.email(),
                        request.senha()
                );

        usuarioAutenticacaoAdapter.definirContexto(authentication);

        String token =
                usuarioAutenticacaoAdapter.gerarToken(authentication);

        String refreshToken = refreshTokenService.criarRefreshToken(usuario);

        UsuarioTokenResponse tokenResponse = UsuarioMapper.toTokenDto(usuario, token, refreshToken);

        return new AutenticacaoUsuarioResponse(usuario, tokenResponse);
    }

    public UsuarioSessaoResponse obterSessao(String email) {
        Usuario usuario = usuarioRepository.findByEmailWithPermissoes(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(email));

        UsuarioTokenResponse dadosUsuario = UsuarioMapper.toTokenDto(usuario, null, null);
        return UsuarioMapper.toSessaoDto(dadosUsuario, usuario);
    }
}
