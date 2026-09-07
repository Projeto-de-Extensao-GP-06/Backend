package br.com.sgpj.repository;

import br.com.sgpj.entity.Otp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface OtpRepository extends JpaRepository<Otp, String> {

    @Modifying
    @Transactional
    void deleteByExpiraEmBefore(LocalDateTime horario);

    Otp findByEmail(String email);

    @Transactional
    void deleteByEmail(String email);
}
