package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.OtpService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DefinirNovaSenha {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public DefinirNovaSenha(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
            PasswordEncoder passwordEncoder,
            OtpService otpService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter =
                usuarioAutenticacaoAdapter;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    public void executar(DefinirNovaSenhaRequest request) {

        if (request.senha() == null || request.senha().isBlank()) {
            throw new IllegalArgumentException(
                    "Insira uma senha válida"
            );
        }

        String email =
                usuarioAutenticacaoAdapter
                        .obterEmailUsuarioLogado();

        Usuario usuario =
                usuarioRepository.findByEmail(email);

        if (usuario == null) {
            throw new UsuarioNaoEncontradoException(email);
        }

        String senhaCriptografada =
                passwordEncoder.encode(request.senha());

        usuario.setSenha(senhaCriptografada);

        usuarioRepository.save(usuario);

        otpService.removeOtp(email);
    }
}