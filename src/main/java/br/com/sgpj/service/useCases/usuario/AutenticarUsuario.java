package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AutenticarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public AutenticarUsuario(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    public AutenticacaoUsuarioResponse executar(UsuarioLoginRequest request) {

        Usuario usuario =
                usuarioRepository.findByEmail(request.email());

        if (usuario == null) {
            throw new UsuarioNaoEncontradoException(request.email());
        }

        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException(
                    "Este usuário está inativo. Contate um administrador."
            );
        }

        Authentication authentication =
                usuarioAutenticacaoAdapter.autenticar(
                        request.email(),
                        request.senha()
                );

        usuarioAutenticacaoAdapter.definirContexto(authentication);

        String token =
                usuarioAutenticacaoAdapter.gerarToken(authentication);

        UsuarioTokenResponse tokenResponse = UsuarioMapper.toTokenDto(usuario, token);

        return new AutenticacaoUsuarioResponse(usuario, tokenResponse);
    }
}