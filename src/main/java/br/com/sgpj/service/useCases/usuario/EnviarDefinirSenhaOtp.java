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

    public StatusEnvioOtp executar(String email) {

        validarEmail(email);

        Usuario usuario =
                usuarioRepository.findByEmail(email);

        if (usuario == null) {
            log.warn("Falha ao enviar código de verificação para definição de senha. Motivo: usuário {} não encontrado.", email);
            return StatusEnvioOtp.USUARIO_NAO_ENCONTRADO;
        }

        if (!usuario.isAtivo()) {
            log.warn("Falha ao enviar código de verificação para definição de senha. Motivo: usuário {} está inativo.", usuario.getId());
            return StatusEnvioOtp.USUARIO_INATIVO;
        }

        if (hasPassword(usuario)) {
            log.warn("Falha ao enviar código de verificação para definição de senha. Motivo: usuário {} já possui senha ativa.", usuario.getId());
            return StatusEnvioOtp.SENHA_JA_CADASTRADA;
        }

        log.info("Enviando código de verificação para usuário {}.", usuario.getId());

        String otp =
                otpService.generateOtp(email);

        emailService.sendSetPasswordOtp(
                email,
                usuario.getNome(),
                otp
        );

        return StatusEnvioOtp.ENVIADO;
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