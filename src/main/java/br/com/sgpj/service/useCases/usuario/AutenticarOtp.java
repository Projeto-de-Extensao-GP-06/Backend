package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AutenticarOtp {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public AutenticarOtp(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter =
                usuarioAutenticacaoAdapter;
    }

    public AutenticacaoUsuarioResponse executar(
            ValidarOtpRequest request
    ) {

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

        UserDetails userDetails =
                User.withUsername(request.email())
                        .password("")
                        .authorities(Collections.emptyList())
                        .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        usuarioAutenticacaoAdapter.definirContexto(
                authentication
        );

        String token =
                usuarioAutenticacaoAdapter.gerarTokenOtp(
                        authentication
                );

        UsuarioTokenResponse tokenResponse = UsuarioMapper.toTokenDto(
                usuario,
                token
        );

        return new AutenticacaoUsuarioResponse(usuario, tokenResponse);
    }
}