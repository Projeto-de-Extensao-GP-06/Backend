package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.EmailService;
import br.com.sgpj.service.OtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnviarRedefinirSenhaOtp Use Case")
class EnviarRedefinirSenhaOtpTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EnviarRedefinirSenhaOtp useCase;

    @Nested
    @DisplayName("Validação de Email")
    class ValidacaoEmail {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar IllegalArgumentException quando o email for inválido")
        void deveLancarExcecaoQuandoEmailInvalido(String emailInvalido) {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.executar(emailInvalido)
            );

            assertEquals("Insira um endereço de e-mail válido", ex.getMessage());
            verify(usuarioRepository, never()).findByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("Cenários de Negócio")
    class CenariosNegocio {

        private final String email = "usuario@teste.com";

        @Test
        @DisplayName("Deve gerar OTP como inválido e não enviar email quando usuário não existir no banco")
        void deveGerarOtpMasNaoEnviarEmailQuandoUsuarioNaoExistir() {
            when(usuarioRepository.findByEmail(email)).thenReturn(null);
            when(otpService.generateOtp(email, false)).thenReturn("123456");

            useCase.executar(email);

            verify(otpService).generateOtp(email, false);
            verify(emailService, never()).sendForgotPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve gerar OTP como inválido e não enviar email quando usuário estiver inativo")
        void deveGerarOtpMasNaoEnviarEmailQuandoUsuarioInativo() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            usuario.setAtivo(false);

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(otpService.generateOtp(email, false)).thenReturn("123456");

            useCase.executar(email);

            verify(otpService).generateOtp(email, false);
            verify(emailService, never()).sendForgotPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve gerar OTP como inválido e não enviar email quando usuário não possuir senha")
        void deveGerarOtpMasNaoEnviarEmailQuandoUsuarioNaoPossuirSenha() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            usuario.setAtivo(true);
            usuario.setSenha(null);

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(otpService.generateOtp(email, false)).thenReturn("123456");

            useCase.executar(email);

            verify(otpService).generateOtp(email, false);
            verify(emailService, never()).sendForgotPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve gerar OTP como válido e enviar email quando usuário for válido e possuir senha")
        void deveEnviarOtpComSucesso() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setNome("Ciclano");
            usuario.setEmail(email);
            usuario.setAtivo(true);
            usuario.setSenha("senha-criptografada");

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(otpService.generateOtp(email, true)).thenReturn("123456");

            useCase.executar(email);

            verify(otpService).generateOtp(email, true);
            verify(emailService).sendForgotPasswordOtp(email, "Ciclano", "123456");
        }

        @Test
        @DisplayName("Deve bloquear envio de OTP de redefinição de senha para o usuário SISTEMA")
        void deveBloquearOtpParaUsuarioSistema() {
            Usuario usuarioSistema = new Usuario();
            usuarioSistema.setId(Usuario.ID_SISTEMA);
            usuarioSistema.setNome(Usuario.NOME_SISTEMA);
            usuarioSistema.setEmail(Usuario.EMAIL_SISTEMA);
            usuarioSistema.setAtivo(true);
            usuarioSistema.setSenha("qualquer");

            when(usuarioRepository.findByEmail(Usuario.EMAIL_SISTEMA)).thenReturn(usuarioSistema);
            when(otpService.generateOtp(Usuario.EMAIL_SISTEMA, false)).thenReturn("123456");

            useCase.executar(Usuario.EMAIL_SISTEMA);

            verify(otpService).generateOtp(Usuario.EMAIL_SISTEMA, false);
            verify(emailService, never()).sendForgotPasswordOtp(anyString(), anyString(), anyString());
        }
    }
}
