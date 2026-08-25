package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.OtpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do OtpPersistenciaAdapter")
class OtpPersistenciaAdapterTest {

    @Mock
    private OtpRepository repository;

    @InjectMocks
    private OtpPersistenciaAdapter adapter;

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final String OTP_CODIGO = "123456";

    @Test
    @DisplayName("Deve salvar OTP no repositório")
    void deveSalvarOtpNoRepositorio() {
        // Given
        Otp otp = criarOtp();
        when(repository.save(otp)).thenReturn(otp);

        // When
        adapter.save(otp);

        // Then
        verify(repository).save(otp);
    }

    @Test
    @DisplayName("Deve chamar repository.save exatamente uma vez")
    void deveChamarRepositorySaveExatamenteUmaVez() {
        // Given
        Otp otp = criarOtp();
        when(repository.save(otp)).thenReturn(otp);

        // When
        adapter.save(otp);

        // Then
        verify(repository, times(1)).save(otp);
    }

    @Test
    @DisplayName("Deve buscar OTP por email quando existir")
    void deveBuscarOtpPorEmailQuandoExistir() {
        // Given
        Otp otp = criarOtp();
        when(repository.findById(EMAIL_TESTE)).thenReturn(Optional.of(otp));

        // When
        Otp resultado = adapter.findByEmail(EMAIL_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(otp, resultado);
        verify(repository).findById(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve retornar OTP com email correto")
    void deveRetornarOtpComEmailCorreto() {
        // Given
        Otp otp = criarOtp();
        when(repository.findById(EMAIL_TESTE)).thenReturn(Optional.of(otp));

        // When
        Otp resultado = adapter.findByEmail(EMAIL_TESTE);

        // Then
        assertEquals(EMAIL_TESTE, resultado.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção quando OTP não for encontrado por email")
    void deveLancarExcecaoQuandoOtpNaoForEncontradoPorEmail() {
        // Given
        when(repository.findById(EMAIL_TESTE)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmail(EMAIL_TESTE)
        );

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        assertFalse(exception.getMessage().contains(EMAIL_TESTE), "Email não deve estar exposto na mensagem de erro");
        verify(repository).findById(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve não incluir email na mensagem de exceção quando não encontrado - segurança")
    void deveNaoIncluirEmailNaMensagemExcecaoQuandoNaoEncontrado() {
        // Given
        String emailEspecifico = "usuario@especifico.com";
        when(repository.findById(emailEspecifico)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmail(emailEspecifico)
        );

        assertFalse(exception.getMessage().contains(emailEspecifico), "Email não deve estar exposto na mensagem de erro");
        assertFalse(exception.getMessage().contains("@"), "Email não deve estar exposto na mensagem de erro");
    }

    @Test
    @DisplayName("Deve deletar OTP por email")
    void deveDeletarOtpPorEmail() {
        // When
        adapter.deleteByEmail(EMAIL_TESTE);

        // Then
        verify(repository).deleteById(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve chamar repository.deleteById exatamente uma vez")
    void deveChamarRepositoryDeleteByIdExatamenteUmaVez() {
        // When
        adapter.deleteByEmail(EMAIL_TESTE);

        // Then
        verify(repository, times(1)).deleteById(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve deletar OTP com email correto")
    void deveDeletarOtpComEmailCorreto() {
        // Given
        String emailParaDeletar = "deletar@email.com";

        // When
        adapter.deleteByEmail(emailParaDeletar);

        // Then
        verify(repository).deleteById(emailParaDeletar);
    }

    @Test
    @DisplayName("Deve deletar OTPs expirados antes da data especificada")
    void deveDeletarOtpsExpiradosAntesDaDataEspecificada() {
        // Given
        LocalDateTime dataLimite = LocalDateTime.now();

        // When
        adapter.deleteExpiredBefore(dataLimite);

        // Then
        verify(repository).deleteByExpiraEmBefore(dataLimite);
    }

    @Test
    @DisplayName("Deve chamar repository.deleteByExpiraEmBefore exatamente uma vez")
    void deveChamarRepositoryDeleteByExpiraEmBeforeExatamenteUmaVez() {
        // Given
        LocalDateTime dataLimite = LocalDateTime.now();

        // When
        adapter.deleteExpiredBefore(dataLimite);

        // Then
        verify(repository, times(1)).deleteByExpiraEmBefore(dataLimite);
    }

    @Test
    @DisplayName("Deve deletar OTPs expirados com data correta")
    void deveDeletarOtpsExpiradosComDataCorreta() {
        // Given
        LocalDateTime dataEspecifica = LocalDateTime.of(2024, 1, 15, 10, 30);

        // When
        adapter.deleteExpiredBefore(dataEspecifica);

        // Then
        verify(repository).deleteByExpiraEmBefore(dataEspecifica);
    }

    @Test
    @DisplayName("Deve permitir deletar múltiplos emails sequencialmente")
    void devePermitirDeletarMultiplosEmailsSequencialmente() {
        // When
        adapter.deleteByEmail("email1@teste.com");
        adapter.deleteByEmail("email2@teste.com");
        adapter.deleteByEmail("email3@teste.com");

        // Then
        verify(repository).deleteById("email1@teste.com");
        verify(repository).deleteById("email2@teste.com");
        verify(repository).deleteById("email3@teste.com");
        verify(repository, times(3)).deleteById(anyString());
    }

    @Test
    @DisplayName("Deve permitir salvar múltiplos OTPs sequencialmente")
    void devePermitirSalvarMultiplosOtpsSequencialmente() {
        // Given
        Otp otp1 = criarOtpComEmail("email1@teste.com");
        Otp otp2 = criarOtpComEmail("email2@teste.com");
        when(repository.save(any(Otp.class))).thenReturn(otp1, otp2);

        // When
        adapter.save(otp1);
        adapter.save(otp2);

        // Then
        verify(repository, times(2)).save(any(Otp.class));
    }

    // Métodos auxiliares

    private Otp criarOtp() {
        return criarOtpComEmail(EMAIL_TESTE);
    }

    private Otp criarOtpComEmail(String email) {
        return new Otp(email, OTP_CODIGO, LocalDateTime.now().plusMinutes(15));
    }
}
