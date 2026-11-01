package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.EmailService;
import br.com.sgpj.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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

        Usuario usuario = usuarioRepository.findByEmail(email);

        boolean usuarioValido = usuario != null && !usuario.isUsuarioSistema() && usuario.isAtivo() && !hasPassword(usuario);
        String otp = otpService.generateOtp(email, usuarioValido);

        if (usuario == null) {
            log.warn("Solicitação de OTP para e-mail não cadastrado: {}. Código gerado para controle de tentativas, mas e-mail não enviado.", email);
            return;
        }

        if (usuario.isUsuarioSistema()) {
            log.warn("Solicitação de OTP bloqueada para o usuário de sistema: {}.", email);
            return;
        }

        if (!usuario.isAtivo()) {
            log.warn("Solicitação de OTP para usuário inativo: {}. Código gerado para controle de tentativas, mas e-mail não enviado.", usuario.getId());
            return;
        }

        if (hasPassword(usuario)) {
            log.warn("Usuário ID: {} já possui senha ativa. Deve redefinir a senha. E-mail de definição não enviado.", usuario.getId());
            return;
        }

        log.info("Enviando código de verificação para usuário {}.", usuario.getId());
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