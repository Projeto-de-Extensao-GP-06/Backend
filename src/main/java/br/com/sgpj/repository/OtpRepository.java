package br.com.sgpj.repository;

import br.com.sgpj.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, String> {
}
