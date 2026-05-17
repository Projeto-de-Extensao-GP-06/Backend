package br.com.sgpj.service;

import br.com.sgpj.adapter.database.OtpPersistenciaAdapter;
import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.LimiteTentativasExcedidoException;
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

    @InjectMocks
    private OtpService otpService;

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final String OTP_VALIDO = "123456";

    @Test
    @DisplayName("Deve gerar OTP com 6 dígitos")
    void deveGerarOtpComSeisDigitos() {
        // When
        String otp = otpService.generateOtp(EMAIL_TESTE);

        // Then
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"));
    }

    @Test
    @DisplayName("Deve salvar OTP ao gerar")
    void deveSalvarOtpAoGerar() {
        // When
        otpService.generateOtp(EMAIL_TESTE);

        // Then
        verify(otpPersistenciaAdapter, times(1)).save(any(Otp.class));
    }

    @Test
    @DisplayName("Deve gerar OTP com tempo de expiração de 15 minutos")
    void deveGerarOtpComTempoExpiracao15Minutos() {
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
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, OTP_VALIDO);

        // Then
        assertTrue(resultado);
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
    }

    @Test
    @DisplayName("Deve retornar falso para OTP incorreto")
    void deveRetornarFalsoParaOtpIncorreto() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, "999999");

        // Then
        assertFalse(resultado);
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
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

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
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(true);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

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
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(true);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

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
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, "  " + OTP_VALIDO + "  ");

        // Then
        assertTrue(resultado);
        verify(otpPersistenciaAdapter, times(1)).deleteByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve tratar OTP nulo")
    void deveTratarOtpNulo() {
        // Given
        Otp otp = mock(Otp.class);
        when(otp.isExpired()).thenReturn(false);
        when(otp.getOtp()).thenReturn(OTP_VALIDO);
        when(otp.excedeuLimiteDeTentativas(3)).thenReturn(false);
        when(otpPersistenciaAdapter.findByEmail(EMAIL_TESTE)).thenReturn(otp);

        // When
        boolean resultado = otpService.isOtpValid(EMAIL_TESTE, null);

        // Then
        assertFalse(resultado);
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
}