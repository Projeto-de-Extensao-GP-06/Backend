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
@DisplayName("EnviarDefinirSenhaOtp Use Case")
class EnviarDefinirSenhaOtpTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EnviarDefinirSenhaOtp useCase;

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
        @DisplayName("Deve retornar USUARIO_NAO_ENCONTRADO quando usuário não existir no banco")
        void deveRetornarUsuarioNaoEncontrado() {
            when(usuarioRepository.findByEmail(email)).thenReturn(null);

            StatusEnvioOtp status = useCase.executar(email);

            assertEquals(StatusEnvioOtp.USUARIO_NAO_ENCONTRADO, status);
            verify(otpService, never()).generateOtp(anyString());
            verify(emailService, never()).sendSetPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve retornar USUARIO_INATIVO quando usuário estiver inativo")
        void deveRetornarUsuarioInativo() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            usuario.setAtivo(false);

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);

            StatusEnvioOtp status = useCase.executar(email);

            assertEquals(StatusEnvioOtp.USUARIO_INATIVO, status);
            verify(otpService, never()).generateOtp(anyString());
            verify(emailService, never()).sendSetPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve retornar SENHA_JA_CADASTRADA quando usuário já possuir senha")
        void deveRetornarSenhaJaCadastrada() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            usuario.setAtivo(true);
            usuario.setSenha("senha-criptografada");

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);

            StatusEnvioOtp status = useCase.executar(email);

            assertEquals(StatusEnvioOtp.SENHA_JA_CADASTRADA, status);
            verify(otpService, never()).generateOtp(anyString());
            verify(emailService, never()).sendSetPasswordOtp(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Deve gerar OTP, enviar email e retornar ENVIADO quando usuário for válido e sem senha")
        void deveEnviarOtpComSucesso() {
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setNome("Fulano");
            usuario.setEmail(email);
            usuario.setAtivo(true);
            usuario.setSenha(null);

            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(otpService.generateOtp(email)).thenReturn("654321");

            StatusEnvioOtp status = useCase.executar(email);

            assertEquals(StatusEnvioOtp.ENVIADO, status);
            verify(otpService).generateOtp(email);
            verify(emailService).sendSetPasswordOtp(email, "Fulano", "654321");
        }
    }
}
