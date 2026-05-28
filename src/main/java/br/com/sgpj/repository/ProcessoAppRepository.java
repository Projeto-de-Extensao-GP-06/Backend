package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long> {
    List<ProcessoApp> findByCliente(Cliente cliente);

    @Query("""
            select p
            from ProcessoApp p
            left join p.cliente c
            left join p.processoApi api
            left join p.faseProcesso fase
            left join p.siglaTribunal tribunal
            where (
                :query is null
                or lower(c.nomeRazaoSocial) like lower(concat('%', :query, '%'))
                or lower(api.numeroCnj) like lower(concat('%', :query, '%'))
            )
            and (:faseId is null or fase.id = :faseId)
            and (:tribunalId is null or tribunal.id = :tribunalId)
            """)
    List<ProcessoApp> buscaFiltro(
            @Param("query") String query,
            @Param("faseId") Long faseId,
            @Param("tribunalId") Long tribunalId);
}
