package br.com.sgpj.repository;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicrosoftTokenRepository extends JpaRepository<MicrosoftTokenConfig, Long> {
}
