package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class DefinirNovaSenha {

    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

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

        if (request == null) {
            throw new IllegalArgumentException(
                    "Insira um payload válido"
            );
        }

        validarSenha(request.senha());

        String email =
                usuarioAutenticacaoAdapter
                        .obterEmailUsuarioLogado();

        Usuario usuario =
                usuarioRepository.findByEmail(email);

        if (usuario == null) {
            log.error("Falha na definição de senha. Motivo: email {} não encontrado.", email);
            throw new UsuarioNaoEncontradoException(email);
        }

        if (usuario.isUsuarioSistema()) {
            log.warn("Tentativa bloqueada de definir senha para o usuário de sistema: {}.", email);
            throw new OperacaoInvalidaException("Não é permitido definir senha para o usuário do sistema.");
        }

        log.info("Definindo nova senha para usuário {}.", usuario.getId());

        String senhaCriptografada =
                passwordEncoder.encode(request.senha());

        usuario.setSenha(senhaCriptografada);

        usuarioRepository.save(usuario);

        otpService.removeOtp(email);
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException(
                    "Insira uma senha válida"
            );
        }
        if (senha.length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve conter pelo menos 8 caracteres."
            );
        }
        if (senha.length() > 255) {
            throw new IllegalArgumentException(
                    "A senha deve conter no máximo 255 caracteres."
            );
        }
        if (!MAIUSCULA.matcher(senha).find()) {
            throw new IllegalArgumentException(
                    "A senha deve conter pelo menos 1 letra maiúscula."
            );
        }
        if (!MINUSCULA.matcher(senha).find()) {
            throw new IllegalArgumentException(
                    "A senha deve conter pelo menos 1 letra minúscula."
            );
        }
        if (!NUMERO.matcher(senha).find()) {
            throw new IllegalArgumentException(
                    "A senha deve conter pelo menos 1 número."
            );
        }
        if (!ESPECIAL.matcher(senha).find()) {
            throw new IllegalArgumentException(
                    "A senha deve conter pelo menos 1 caractere especial."
            );
        }
    }
}