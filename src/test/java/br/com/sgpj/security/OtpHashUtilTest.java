package br.com.sgpj.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para utilitário de hash de OTP.
 *
 * <p>Esta classe valida que a implementação de hash BCrypt para OTPs
 * funciona corretamente e previne armazenamento em texto puro.</p>
 */
@DisplayName("OtpHashUtil - Testes de Hash de OTP")
class OtpHashUtilTest {

    @Nested
    @DisplayName("Hash de OTP")
    class HashOtpTest {

        @Test
        @DisplayName("Deve gerar hash diferente para cada OTP")
        void deveGerarHashDiferenteParaCadaOtp() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp1 = "123456";
            String otp2 = "123456";

            String hash1 = otpHashUtil.hashOtp(otp1);
            String hash2 = otpHashUtil.hashOtp(otp2);

            // BCrypt gera hashes diferentes mesmo para o mesmo valor (devido ao salt)
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Deve lançar exceção quando OTP for nulo")
        void deveLancarExcecaoQuandoOtpForNulo() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();

            assertThrows(IllegalArgumentException.class, () -> otpHashUtil.hashOtp(null));
        }

        @Test
        @DisplayName("Deve lançar exceção quando OTP for vazio")
        void deveLancarExcecaoQuandoOtpForVazio() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();

            assertThrows(IllegalArgumentException.class, () -> otpHashUtil.hashOtp(""));
        }

        @Test
        @DisplayName("Deve lançar exceção quando OTP for apenas espaços")
        void deveLancarExcecaoQuandoOtpForApenasEspacos() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();

            assertThrows(IllegalArgumentException.class, () -> otpHashUtil.hashOtp("   "));
        }

        @Test
        @DisplayName("Deve gerar hash com tamanho apropriado")
        void deveGerarHashComTamanhoApropriado() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp = "123456";

            String hash = otpHashUtil.hashOtp(otp);

            // BCrypt hashes têm 60 caracteres
            assertEquals(60, hash.length());
        }
    }

    @Nested
    @DisplayName("Verificação de OTP")
    class VerifyOtpTest {

        @Test
        @DisplayName("Deve verificar OTP corretamente")
        void deveVerificarOtpCorretamente() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp = "123456";
            String hash = otpHashUtil.hashOtp(otp);

            assertTrue(otpHashUtil.verifyOtp(otp, hash));
        }

        @Test
        @DisplayName("Deve rejeitar OTP incorreto")
        void deveRejeitarOtpIncorreto() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp = "123456";
            String hash = otpHashUtil.hashOtp(otp);

            assertFalse(otpHashUtil.verifyOtp("654321", hash));
        }

        @Test
        @DisplayName("Deve retornar false quando OTP for nulo")
        void deveRetornarFalseQuandoOtpForNulo() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String hash = "hashed_otp";

            assertFalse(otpHashUtil.verifyOtp(null, hash));
        }

        @Test
        @DisplayName("Deve retornar false quando hash for nulo")
        void deveRetornarFalseQuandoHashForNulo() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp = "123456";

            assertFalse(otpHashUtil.verifyOtp(otp, null));
        }

        @Test
        @DisplayName("Deve retornar false quando ambos forem nulos")
        void deveRetornarFalseQuandoAmbosForemNulos() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();

            assertFalse(otpHashUtil.verifyOtp(null, null));
        }

        @Test
        @DisplayName("Deve verificar OTP com espaços")
        void deveVerificarOtpComEspacos() {
            OtpHashUtil otpHashUtil = new OtpHashUtil();
            String otp = "123456";
            String hash = otpHashUtil.hashOtp(otp);

            // OTP com espaços não deve corresponder
            assertFalse(otpHashUtil.verifyOtp(" 123456 ", hash));
        }
    }
}
