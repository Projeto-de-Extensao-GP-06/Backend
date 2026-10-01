package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.UsuarioRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DefinirNovaSenha - Testes de Validação de Senha")
class DefinirNovaSenhaTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private DefinirNovaSenha definirNovaSenha;

    @Nested
    @DisplayName("Validação de Senha Nula ou em Branco")
    class ValidacaoSenhaNulaOuBranca {

        @ParameterizedTest(name = "Deve rejeitar senha: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar IllegalArgumentException quando senha for nula, vazia ou apenas espaços")
        void deveFalharQuandoSenhaNulaOuEmBranco(String senhaInvalida) {
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest(senhaInvalida);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("Insira uma senha válida", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando request for nulo")
        void deveFalharQuandoRequestForNulo() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(null)
            );

            assertEquals("Insira um payload válido", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validação de Tamanho da Senha")
    class ValidacaoTamanhoSenha {

        @Test
        @DisplayName("Deve falhar quando senha tiver menos de 8 caracteres")
        void deveFalharQuandoSenhaTiverMenosDe8Caracteres() {
            // 7 caracteres com todos os tipos requeridos
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("Aa1!xyz");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter pelo menos 8 caracteres.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando senha tiver mais de 255 caracteres")
        void deveFalharQuandoSenhaTiverMaisDe255Caracteres() {
            // 256 caracteres
            String senha256 = "Aa1!" + "x".repeat(252);
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest(senha256);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter no máximo 255 caracteres.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validação de Complexidade de Caracteres")
    class ValidacaoComplexidadeCaracteres {

        @Test
        @DisplayName("Deve falhar quando senha não contiver pelo menos 1 letra maiúscula")
        void deveFalharQuandoNaoConterMaiuscula() {
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("senha123!@#");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter pelo menos 1 letra maiúscula.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando senha não contiver pelo menos 1 letra minúscula")
        void deveFalharQuandoNaoConterMinuscula() {
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("SENHA123!@#");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter pelo menos 1 letra minúscula.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando senha não contiver pelo menos 1 número")
        void deveFalharQuandoNaoConterNumero() {
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("SenhaSemNumero!@#");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter pelo menos 1 número.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando senha não contiver pelo menos 1 caractere especial")
        void deveFalharQuandoNaoConterCaractereEspecial() {
            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest("SenhaSemEspecial123");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> definirNovaSenha.executar(request)
            );

            assertEquals("A senha deve conter pelo menos 1 caractere especial.", ex.getMessage());
            verify(usuarioRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cenários de Sucesso com Senha Válida")
    class CenariosDeSucesso {

        @Test
        @DisplayName("Deve passar na validação e executar redefinição com senha no limite mínimo (8 caracteres)")
        void devePassarComSenhaNoLimiteMinimo() {
            String email = "usuario@teste.com";
            String senha8Chars = "Aa1!5678";
            Usuario usuario = new Usuario();
            usuario.setId(1L);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(passwordEncoder.encode(senha8Chars)).thenReturn("encodedHash");

            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest(senha8Chars);

            assertDoesNotThrow(() -> definirNovaSenha.executar(request));

            assertEquals("encodedHash", usuario.getSenha());
            verify(usuarioRepository).save(usuario);
            verify(otpService).removeOtp(email);
        }

        @Test
        @DisplayName("Deve passar na validação e executar redefinição com senha no limite máximo (255 caracteres)")
        void devePassarComSenhaNoLimiteMaximo() {
            String email = "usuario@teste.com";
            String senha255Chars = "Aa1!" + "x".repeat(251);
            Usuario usuario = new Usuario();
            usuario.setId(1L);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(usuario);
            when(passwordEncoder.encode(senha255Chars)).thenReturn("encodedHash");

            DefinirNovaSenhaRequest request = new DefinirNovaSenhaRequest(senha255Chars);

            assertDoesNotThrow(() -> definirNovaSenha.executar(request));

            assertEquals("encodedHash", usuario.getSenha());
            verify(usuarioRepository).save(usuario);
            verify(otpService).removeOtp(email);
        }
    }
}
