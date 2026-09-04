package br.com.sgpj.service;

import br.com.sgpj.adapter.database.OtpPersistenciaAdapter;
import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
import br.com.sgpj.exception.OtpGenerationLimitExceededException;
import br.com.sgpj.security.OtpHashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OtpService")
class OtpServiceTest {

    @Mock
    private OtpPersistenciaAdapter otpPersistenciaAdapter;

    @Mock
    private OtpHashUtil otpHashUtil;

    @InjectMocks
    private OtpService otpService;

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final String OTP_VALIDO = "123456";
    private static final String HASH_OTP_VALIDO = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Test
    @DisplayName("Deve gerar OTP com 6 dígitos")
    void deveGerarOtpComSeisDigitos() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When
        String otp = otpService.generateOtp(EMAIL_TESTE);

        // Then
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
        verify(otpHashUtil, times(1)).hashOtp(otp);
    }

    @Test
    @DisplayName("Deve salvar OTP ao gerar")
    void deveSalvarOtpAoGerar() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When
        otpService.generateOtp(EMAIL_TESTE);

        // Then
        verify(otpPersistenciaAdapter, times(1)).save(any(Otp.class));
    }

    @Test
    @DisplayName("Deve gerar OTP com tempo de expiração de 15 minutos")
    void deveGerarOtpComTempoExpiracao15Minutos() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When
        otpService.generateOtp(EMAIL_TESTE);

        // Then
        verify(otpPersistenciaAdapter).save(argThat(otp -> {
            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime expiracao = otp.getExpiraEm();
            long minutosDiferenca = java.time.Duration.between(agora, expiracao).toMinutes();
            return minutosDiferenca >= 14 && minutosDiferenca <= 15;
        }));
    }

    @Test
    @DisplayName("Deve validar OTP correto e não expirado")
    void deveValidarOtpCorretoENaoExpirado() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp(OTP_VALIDO, HASH_OTP_VALIDO)).thenReturn(true);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, OTP_VALIDO);

        // Then
        assertTrue(resultado);
        verify(otpHashUtil, times(1)).verifyOtp(OTP_VALIDO, HASH_OTP_VALIDO);
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve retornar falso para OTP expirado")
    void deveRetornarFalsoParaOtpExpirado() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(true);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, OTP_VALIDO);

        // Then
        assertFalse(resultado);
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
        verifyNoInteractions(otpHashUtil);
    }

    @Test
    @DisplayName("Deve retornar falso para OTP incorreto")
    void deveRetornarFalsoParaOtpIncorreto() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp("999999", HASH_OTP_VALIDO)).thenReturn(false);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, "999999");

        // Then
        assertFalse(resultado);
        verify(otpHashUtil, times(1)).verifyOtp("999999", HASH_OTP_VALIDO);
        verify(otp, times(1)).registrarTentativaIncorreta();
        verify(otpPersistenciaAdapter, times(1)).save(otp);
        verify(otpPersistenciaAdapter, never()).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve registrar tentativa incorreta ao validar OTP errado")
    void deveRegistrarTentativaIncorretaAoValidarOtpErrado() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp("999999", HASH_OTP_VALIDO)).thenReturn(false);

        // When
        otpService.isOtpValid(EMAIL_TESTE, "999999");

        // Then
        verify(otp, times(1)).registrarTentativaIncorreta();
    }

    @Test
    @DisplayName("Deve lançar exceção ao exceder limite de tentativas")
    void deveLancarExcecaoAoExcederLimiteTentativas() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(true);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp("999999", HASH_OTP_VALIDO)).thenReturn(false);

        // When/Then
        LimiteTentativasExcedidoException exception = assertThrows(
                LimiteTentativasExcedidoException.class,
                () -> otpService.isOtpValid(EMAIL_TESTE, "999999")
        );

        assertTrue(exception.getMessage().contains("Número máximo de tentativas alcançado"));
        verify(otp, times(1)).registrarTentativaIncorreta();
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve remover OTP ao exceder tentativas")
    void deveRemoverOtpAoExcederTentativas() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(true);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp("999999", HASH_OTP_VALIDO)).thenReturn(false);

        // When/Then
        assertThrows(
                LimiteTentativasExcedidoException.class,
                () -> otpService.isOtpValid(EMAIL_TESTE, "999999")
        );

        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve validar OTP com espaços em branco")
    void deveValidarOtpComEspacosEmBranco() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp(OTP_VALIDO, HASH_OTP_VALIDO)).thenReturn(true);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, "  " + OTP_VALIDO + "  ");

        // Then
        assertTrue(resultado);
        verify(otpHashUtil, times(1)).verifyOtp(OTP_VALIDO, HASH_OTP_VALIDO);
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve tratar OTP nulo")
    void deveTratarOtpNulo() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(HASH_OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);
        when(otpHashUtil.verifyOtp(null, HASH_OTP_VALIDO)).thenReturn(false);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, null);

        // Then
        assertFalse(resultado);
        verify(otpHashUtil, times(1)).verifyOtp(null, HASH_OTP_VALIDO);
        verify(otp, times(1)).registrarTentativaIncorreta();
    }

    @Test
    @DisplayName("Deve remover OTP por email")
    void deveRemoverOtpPorEmail() {
        // When
        otpService.removeOtp(EMAIL_TESTE);

        // Then
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve executar limpeza de OTPs expirados")
    void deveExecutarLimpezaOtpsExpirados() {
        // When
        otpService.executeCleanup();

        // Then
        verify(otpPersistenciaAdapter, times(1)).deleteExpiredBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve chamar deleteExpiredBefore com data/hora atual ou anterior")
    void deveChamarDeleteExpiredBeforeComDataHoraAtual() {
        // Given
        LocalDateTime antes = LocalDateTime.now();

        // When
        otpService.executeCleanup();

        // Then
        verify(otpPersistenciaAdapter).deleteExpiredBefore(argThat(dataHora -> {
            LocalDateTime depois = LocalDateTime.now();
            return !dataHora.isBefore(antes) && !dataHora.isAfter(depois);
        }));
    }

    @Test
    @DisplayName("Deve permitir geração de OTP dentro do limite por minuto")
    void devePermitirGeracaoOtpDentroLimiteMinuto() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When - Should allow up to MAX_OTP_GENERATION_PER_MINUTE (2) requests
        assertDoesNotThrow(() -> otpService.generateOtp(EMAIL_TESTE));
        assertDoesNotThrow(() -> otpService.generateOtp(EMAIL_TESTE));

        // Then
        verify(otpHashUtil, times(2)).hashOtp(any(String.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando limite de geração por minuto é excedido")
    void deveLancarExcecaoQuandoLimiteGeracaoMinutoExcedido() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When - Make 2 requests (max per minute)
        otpService.generateOtp(EMAIL_TESTE);
        otpService.generateOtp(EMAIL_TESTE);

        // Then - 3rd request should exceed limit
        assertThrows(OtpGenerationLimitExceededException.class, () -> otpService.generateOtp(EMAIL_TESTE));
    }

    @Test
    @DisplayName("Deve isolar limites de geração por email")
    void deveIsolarLimitesGeracaoPorEmail() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);
        String email1 = "user1@test.com";
        String email2 = "user2@test.com";

        // When - Max out requests for email1
        otpService.generateOtp(email1);
        otpService.generateOtp(email1);

        // Then - email1 should be rate limited
        assertThrows(OtpGenerationLimitExceededException.class, () -> otpService.generateOtp(email1));

        // But email2 should still be able to generate OTP
        assertDoesNotThrow(() -> otpService.generateOtp(email2));
    }

    @Test
    @DisplayName("Deve ser case-insensitive e trim para email")
    void deveSerCaseInsensitiveETrimParaEmail() {
        // Given
        when(otpHashUtil.hashOtp(any(String.class))).thenReturn(HASH_OTP_VALIDO);

        // When - Generate with different cases and spacing
        otpService.generateOtp("  TEST@EMAIL.COM  ");
        otpService.generateOtp("test@email.com");

        // Then - Should be treated as same email and rate limited
        assertThrows(OtpGenerationLimitExceededException.class, () -> otpService.generateOtp("TEST@EMAIL.COM"));
    }
}