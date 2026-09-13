package br.com.sgpj.repository;

import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MotivoStatusProcessoRepository extends JpaRepository<MotivoStatusProcesso, Integer> {
    List<MotivoStatusProcesso> findByAplicavelA(MotivoStatusProcesso.AplicavelA aplicavelA);
}
