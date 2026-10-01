package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.exception.OtpInvalidaException;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidarOtp Use Case")
class ValidarOtpTest {

    @Mock
    private OtpService otpService;

    @InjectMocks
    private ValidarOtp useCase;

    @Nested
    @DisplayName("Validação do Request")
    class ValidacaoRequest {

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando request for nulo")
        void deveLancarExcecaoQuandoRequestNulo() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.executar(null)
            );

            assertEquals("Insira um payload válido.", ex.getMessage());
            verify(otpService, never()).isOtpValid(anyString(), anyString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar IllegalArgumentException quando o email for inválido")
        void deveLancarExcecaoQuandoEmailInvalido(String emailInvalido) {
            ValidarOtpRequest request = new ValidarOtpRequest(emailInvalido, "123456");

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.executar(request)
            );

            assertEquals("Insira um endereço de e-mail válido", ex.getMessage());
            verify(otpService, never()).isOtpValid(anyString(), anyString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Deve lançar IllegalArgumentException quando o otp for inválido")
        void deveLancarExcecaoQuandoOtpInvalido(String otpInvalido) {
            ValidarOtpRequest request = new ValidarOtpRequest("usuario@teste.com", otpInvalido);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.executar(request)
            );

            assertEquals("Insira uma OTP válida.", ex.getMessage());
            verify(otpService, never()).isOtpValid(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Validação do Código OTP")
    class ValidacaoCodigoOtp {

        private final String email = "usuario@teste.com";
        private final String otp = "123456";

        @Test
        @DisplayName("Deve lançar OtpInvalidaException quando o código OTP for inválido")
        void deveLancarOtpInvalidaExceptionQuandoOtpInvalido() {
            ValidarOtpRequest request = new ValidarOtpRequest(email, otp);
            when(otpService.isOtpValid(email, otp)).thenReturn(false);

            OtpInvalidaException ex = assertThrows(
                    OtpInvalidaException.class,
                    () -> useCase.executar(request)
            );

            assertEquals("Código de verificação inválido.", ex.getMessage());
            verify(otpService).isOtpValid(email, otp);
        }

        @Test
        @DisplayName("Deve executar com sucesso quando o código OTP for válido")
        void deveValidarOtpComSucesso() {
            ValidarOtpRequest request = new ValidarOtpRequest(email, otp);
            when(otpService.isOtpValid(email, otp)).thenReturn(true);

            assertDoesNotThrow(() -> useCase.executar(request));
            verify(otpService).isOtpValid(email, otp);
        }
    }
}
