package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Otp;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpPersistenciaAdapter {

    private final OtpRepository repository;

    public void save(Otp usuarioOtp) {
        repository.save(usuarioOtp);
    }

    public Otp findByEmail(String email) {
        return repository.findById(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado para esse endereço de email: " + email));
    }

    public void deleteByEmail(String email) {
        repository.deleteById(email);
    }

    public void deleteExpiredBefore(LocalDateTime dateTime) {
        repository.deleteByExpiraEmBefore(dateTime);
    }
}
