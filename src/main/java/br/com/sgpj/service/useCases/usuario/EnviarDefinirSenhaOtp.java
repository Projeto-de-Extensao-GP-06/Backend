package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.PasswordConflictException;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.EmailService;
import br.com.sgpj.service.OtpService;
import org.springframework.stereotype.Service;

@Service
public class EnviarDefinirSenhaOtp {

    private final UsuarioRepository usuarioRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public EnviarDefinirSenhaOtp(
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

        if (!usuario.isAtivo()) {
            throw new UsuarioInativoException(
                    "Este usuário está inativo. Contate um administrador."
            );
        }

        if (hasPassword(usuario)) {
            throw new PasswordConflictException(
                    "Esta conta já possui uma senha cadastrada. Utilize a opção de recuperação de senha."
            );
        }

        String otp =
                otpService.generateOtp(email);

        emailService.sendSetPasswordOtp(
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