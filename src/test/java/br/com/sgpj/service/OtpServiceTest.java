package br.com.sgpj.service;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
import br.com.sgpj.repository.OtpRepository;
import br.com.sgpj.security.OtpHashUtil;
import br.com.sgpj.service.otp.AttemptPolicy;
import br.com.sgpj.service.otp.OtpRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OtpService")
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private OtpHashUtil otpHashUtil;

    @Mock
    private OtpRateLimiter otpRateLimiter;

    private AttemptPolicy attemptPolicy;

    private OtpService otpService;

    private static final String EMAIL = "usuario@sgpj.com.br";

    @BeforeEach
    void setUp() {
        attemptPolicy = new AttemptPolicy(3);
        otpService = new OtpService(otpRepository, otpHashUtil, otpRateLimiter, attemptPolicy);
    }

    @Nested
    @DisplayName("generateOtp")
    class GenerateOtpTest {

        @Test
        @DisplayName("Deve gerar um OTP de 6 dígitos, salvar o hash e registrar a geração")
        void deveGerarOtpEArmazenarHash() {
            when(otpHashUtil.hashOtp(anyString())).thenReturn("hash-fake");

            String otpGerado = otpService.generateOtp(EMAIL);

            assertNotNull(otpGerado);
            assertEquals(6, otpGerado.length());
            assertTrue(otpGerado.chars().allMatch(Character::isDigit));

            verify(otpRateLimiter).checkGenerationAllowed(EMAIL);
            verify(otpHashUtil).hashOtp(otpGerado);
            verify(otpRateLimiter).recordGeneration(EMAIL);

            ArgumentCaptor<Otp> captor = ArgumentCaptor.forClass(Otp.class);
            verify(otpRepository).save(captor.capture());

            Otp otpSalvo = captor.getValue();
            assertEquals(EMAIL, otpSalvo.getEmail());
            assertEquals("hash-fake", otpSalvo.getOtp());
            assertTrue(otpSalvo.getExpiraEm().isAfter(LocalDateTime.now()));
        }

        @Test
        @DisplayName("Deve propagar a exceção quando o rate limiter bloquear a geração")
        void devePropagarExcecaoDeRateLimit() {
            doThrow(new RuntimeException("limite excedido"))
                    .when(otpRateLimiter).checkGenerationAllowed(EMAIL);

            assertThrows(RuntimeException.class, () -> otpService.generateOtp(EMAIL));

            verify(otpRepository, never()).save(any());
            verify(otpRateLimiter, never()).recordGeneration(anyString());
        }
    }

    @Nested
    @DisplayName("isOtpValid")
    class IsOtpValidTest {

        @Test
        @DisplayName("Deve retornar false quando não existir OTP para o email")
        void deveRetornarFalseQuandoOtpNaoExiste() {
            when(otpRepository.findByEmail(EMAIL)).thenReturn(null);

            boolean resultado = otpService.isOtpValid(EMAIL, "123456");

            assertFalse(resultado);
            verify(otpHashUtil, never()).verifyOtp(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve remover o OTP e retornar false quando estiver expirado")
        void deveRetornarFalseQuandoOtpExpirado() {
            Otp otpExpirado = new Otp(EMAIL, "hash", LocalDateTime.now().minusMinutes(1));
            when(otpRepository.findByEmail(EMAIL)).thenReturn(otpExpirado);

            boolean resultado = otpService.isOtpValid(EMAIL, "123456");

            assertFalse(resultado);
            verify(otpRepository).deleteByEmail(EMAIL);
            verify(otpHashUtil, never()).verifyOtp(anyString(), anyString());
        }

        @Test
        @DisplayName("Deve retornar true, remover trailing spaces e apagar o OTP quando válido")
        void deveRetornarTrueQuandoOtpValido() {
            Otp otpValido = new Otp(EMAIL, "hash-armazenado", LocalDateTime.now().plusMinutes(10));
            when(otpRepository.findByEmail(EMAIL)).thenReturn(otpValido);
            when(otpHashUtil.verifyOtp("123456", "hash-armazenado")).thenReturn(true);

            boolean resultado = otpService.isOtpValid(EMAIL, "  123456  ");

            assertTrue(resultado);
            verify(otpHashUtil).verifyOtp("123456", "hash-armazenado");
            verify(otpRepository).deleteByEmail(EMAIL);
        }

        @Test
        @DisplayName("Deve retornar false, registrar tentativa e persistir quando o código for incorreto")
        void deveRegistrarTentativaIncorretaQuandoOtpInvalido() {
            Otp otp = new Otp(EMAIL, "hash-armazenado", LocalDateTime.now().plusMinutes(10), 0);
            when(otpRepository.findByEmail(EMAIL)).thenReturn(otp);
            when(otpHashUtil.verifyOtp("000000", "hash-armazenado")).thenReturn(false);

            boolean resultado = otpService.isOtpValid(EMAIL, "000000");

            assertFalse(resultado);
            assertEquals(1, otp.getTentativas());
            verify(otpRepository, never()).deleteByEmail(EMAIL);
            verify(otpRepository).save(otp);
        }

        @Test
        @DisplayName("Deve lançar LimiteTentativasExcedidoException ao esgotar as tentativas permitidas")
        void deveLancarExcecaoQuandoExcederLimiteDeTentativas() {
            // maxTentativas = 3; já houve 2 tentativas incorretas anteriores
            Otp otp = new Otp(EMAIL, "hash-armazenado", LocalDateTime.now().plusMinutes(10), 2);
            when(otpRepository.findByEmail(EMAIL)).thenReturn(otp);
            when(otpHashUtil.verifyOtp("000000", "hash-armazenado")).thenReturn(false);

            assertThrows(LimiteTentativasExcedidoException.class,
                    () -> otpService.isOtpValid(EMAIL, "000000"));

            assertEquals(3, otp.getTentativas());
            verify(otpRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve tratar um input OTP nulo sem lançar NullPointerException")
        void deveTratarInputOtpNulo() {
            Otp otp = new Otp(EMAIL, "hash-armazenado", LocalDateTime.now().plusMinutes(10), 0);
            when(otpRepository.findByEmail(EMAIL)).thenReturn(otp);
            when(otpHashUtil.verifyOtp(null, "hash-armazenado")).thenReturn(false);

            boolean resultado = otpService.isOtpValid(EMAIL, null);

            assertFalse(resultado);
        }
    }

    @Nested
    @DisplayName("removeOtp")
    class RemoveOtpTest {

        @Test
        @DisplayName("Deve delegar a remoção ao repositório")
        void deveDelegarRemocaoAoRepositorio() {
            otpService.removeOtp(EMAIL);

            verify(otpRepository).deleteByEmail(EMAIL);
        }
    }
}
