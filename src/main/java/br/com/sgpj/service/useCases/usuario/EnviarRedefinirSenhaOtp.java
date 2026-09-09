package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.PasswordConflictException;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.EmailService;
import br.com.sgpj.service.OtpService;
import org.springframework.stereotype.Service;

@Service
public class EnviarRedefinirSenhaOtp {

    private final UsuarioRepository usuarioRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public EnviarRedefinirSenhaOtp(
            UsuarioRepository usuarioRepository,
            OtpService otpService,
            EmailService emailService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public void executar(String email) {

        validarEmail(email);

        Usuario usuario =
                usuarioRepository.findByEmail(email);

        if (usuario == null) {
            throw new UsuarioNaoEncontradoException(email);
        }

        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException(
                    "Este usuário está inativo. Contate um administrador."
            );
        }

        if (!hasPassword(usuario)) {
            throw new PasswordConflictException(
                    "Essa conta ainda não realizou o primeiro acesso no sistema e não possui uma senha ativa."
            );
        }

        String otp =
                otpService.generateOtp(email);

        emailService.sendForgotPasswordOtp(
                email,
                usuario.getNome(),
                otp
        );
    }

    private void validarEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Insira um endereço de e-mail válido"
            );
        }
    }

    private boolean hasPassword(Usuario usuario) {
        return usuario.getSenha() != null
                && !usuario.getSenha().isBlank();
    }
}