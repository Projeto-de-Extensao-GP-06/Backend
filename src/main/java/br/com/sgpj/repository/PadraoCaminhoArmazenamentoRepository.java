package br.com.sgpj.repository;

import br.com.sgpj.entity.PadraoCaminhoArmazenamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PadraoCaminhoArmazenamentoRepository extends JpaRepository<PadraoCaminhoArmazenamento, Long> {

    Optional<PadraoCaminhoArmazenamento> findByNome(String nome);
}

