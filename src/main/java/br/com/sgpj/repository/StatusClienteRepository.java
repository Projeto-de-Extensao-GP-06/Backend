package br.com.sgpj.repository;

import br.com.sgpj.entity.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusClienteRepository extends JpaRepository<StatusCliente, Long> {
}

