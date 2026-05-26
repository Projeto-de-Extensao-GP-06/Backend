package br.com.sgpj.repository;

import br.com.sgpj.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findByOnedriveItemId(String onedriveItemId);

    List<Documento> findByCriadoPorUsuarioIdAndAtivoTrue(Long usuarioId);

    List<Documento> findByAtivoTrue();

    Optional<Documento> findByPadraoCaminhoArmazenamento(PadraoCaminhoArmazenamento padraoCaminhoArmazenamento);

    boolean existsByNomeOriginal(String nomeOriginal);

    boolean existsByOnedriveItemId(String id);

    Optional<Documento> findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue(String nomeOriginal, Long padraoArmazenamentoId);

    List<Documento> findByPadraoArmazenamentoIdAndAtivoTrue(Long padraoArmazenamentoId);

    List<Documento> findByPadraoArmazenamentoId(Long padraoArmazenamentoId);
}

