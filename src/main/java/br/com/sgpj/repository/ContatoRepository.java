package br.com.sgpj.repository;

import br.com.sgpj.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {
}

