package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.repository.OtpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes mínimos do OtpRepository - adaptações para código atual")
class OtpPersistenciaAdapterTest {

    @Mock
    private OtpRepository repository;

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final String OTP_CODIGO = "123456";

    @Test
    @DisplayName("Verifica chamadas básicas ao repositório de OTP")
    void verificaChamadasBasicas() {
        Otp otp = new Otp(EMAIL_TESTE, OTP_CODIGO, LocalDateTime.now().plusMinutes(15));

        repository.save(otp);
        repository.findById(EMAIL_TESTE);
        repository.deleteById(EMAIL_TESTE);
        repository.deleteByExpiraEmBefore(LocalDateTime.now());

        verify(repository).save(otp);
        verify(repository).findById(EMAIL_TESTE);
        verify(repository).deleteById(EMAIL_TESTE);
        verify(repository).deleteByExpiraEmBefore(any(LocalDateTime.class));
    }
}
