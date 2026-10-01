package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
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
            log.error("Falha na autenticação de usuário. Motivo: usuário {} não encontrado.", request.email());
            throw new BadCredentialsException("E-mail e/ou senha inválido(s).");
        }

        if (!usuario.isAtivo()) {
            log.error("Falha na autenticação de usuário. Motivo: usuário {} inativo.", request.email());
            throw new BadCredentialsException("E-mail e/ou senha inválido(s).");
        }

        log.info("Autenticando usuário {} para alteração/definição de senha.", request.email());

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
                token,
                "no-refresh-token"
        );

        return new AutenticacaoUsuarioResponse(usuario, tokenResponse);
    }
}