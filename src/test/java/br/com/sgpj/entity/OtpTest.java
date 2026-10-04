package br.com.sgpj.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da Entidade Otp")
class OtpTest {

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final String OTP_VALIDO = "123456";

    @Test
    @DisplayName("Deve inicializar OTP corretamente com o construtor personalizado")
    void deveInicializarOtpCorretamente() {
        // Given
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(15);

        // When
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, expiraEm);

        // Then
        assertEquals(EMAIL_TESTE, otp.getEmail());
        assertEquals(OTP_VALIDO, otp.getOtp());
        assertEquals(expiraEm, otp.getExpiraEm());
        assertEquals(0, otp.getTentativas());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro para OTP expirado (data no passado)")
    void deveRetornarVerdadeiroParaOtpExpirado() {
        // Given
        LocalDateTime expiraEm = LocalDateTime.now().minusMinutes(1);
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, expiraEm);

        // When
        boolean expirado = otp.isExpired();

        // Then
        assertTrue(expirado);
    }

    @Test
    @DisplayName("Deve retornar falso para OTP dentro da validade (data no futuro)")
    void deveRetornarFalsoParaOtpDentroDaValidade() {
        // Given
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(5);
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, expiraEm);

        // When
        boolean expirado = otp.isExpired();

        // Then
        assertFalse(expirado);
    }

    @Test
    @DisplayName("Deve incrementar o contador de tentativas ao registrar tentativa incorreta")
    void deveIncrementarTentativasAoRegistrarTentativaIncorreta() {
        // Given
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, LocalDateTime.now().plusMinutes(15));
        assertEquals(0, otp.getTentativas());

        // When
        otp.registrarTentativaIncorreta();
        otp.registrarTentativaIncorreta();

        // Then
        assertEquals(2, otp.getTentativas());
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando atingir o limite exato de tentativas")
    void deveRetornarVerdadeiroQuandoAtingirLimiteTentativas() {
        // Given
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, LocalDateTime.now().plusMinutes(15));
        otp.setTentativas(3);
        int limiteTentativas = 3;

        // When
        boolean excedeu = otp.excedeuLimiteDeTentativas(limiteTentativas);

        // Then
        assertTrue(excedeu);
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando ultrapassar o limite de tentativas")
    void deveRetornarVerdadeiroQuandoUltrapassarLimiteTentativas() {
        // Given
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, LocalDateTime.now().plusMinutes(15));
        otp.setTentativas(4);
        int limiteTentativas = 3;

        // When
        boolean excedeu = otp.excedeuLimiteDeTentativas(limiteTentativas);

        // Then
        assertTrue(excedeu);
    }

    @Test
    @DisplayName("Deve retornar falso quando não atingir o limite de tentativas")
    void deveRetornarFalsoQuandoNaoAtingirLimiteTentativas() {
        // Given
        Otp otp = new Otp(EMAIL_TESTE, OTP_VALIDO, LocalDateTime.now().plusMinutes(15));
        otp.setTentativas(2);
        int limiteTentativas = 3;

        // When
        boolean excedeu = otp.excedeuLimiteDeTentativas(limiteTentativas);

        // Then
        assertFalse(excedeu);
    }

    @Test
    @DisplayName("Deve inicializar e manipular usuarioValido corretamente")
    void deveManipularUsuarioValidoCorretamente() {
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(15);
        Otp otpValido = new Otp(EMAIL_TESTE, OTP_VALIDO, expiraEm, true);
        assertTrue(otpValido.isUsuarioValido());

        Otp otpInvalido = new Otp(EMAIL_TESTE, OTP_VALIDO, expiraEm, false);
        assertFalse(otpInvalido.isUsuarioValido());

        otpInvalido.setUsuarioValido(true);
        assertTrue(otpInvalido.isUsuarioValido());
    }
}