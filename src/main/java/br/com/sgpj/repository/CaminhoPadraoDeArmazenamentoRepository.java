package br.com.sgpj.repository;

import br.com.sgpj.entity.CaminhoPadraoDeArmazenamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaminhoPadraoDeArmazenamentoRepository extends JpaRepository<CaminhoPadraoDeArmazenamento, Long> {
    List<CaminhoPadraoDeArmazenamento> findByClienteId(Long clienteId);
    boolean existsByCaminho(String caminho);
}
