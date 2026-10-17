package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long>, JpaSpecificationExecutor<ProcessoApp> {
    List<ProcessoApp> findByCliente(Cliente cliente);

    List<ProcessoApp> findByClienteId(Long clienteId);

    List<ProcessoApp> findByCliente_StatusCliente_NomeIgnoreCaseOrderByDataHoraInicioDesc(String statusNome);

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

    @Query("""
            select count(p) > 0
            from ProcessoApp p
            join p.processoApi api
            where api.numeroCnj is not null
              and (
                  api.numeroCnj = :cnjLimpo
                  or replace(replace(replace(api.numeroCnj, '.', ''), '-', ''), '/', '') = :cnjLimpo
              )
            """)
    boolean existsByCnj(@Param("cnjLimpo") String cnjLimpo);

    @Query("""
            select count(p) > 0
            from ProcessoApp p
            join p.processoApi api
            where p.id <> :id
              and api.numeroCnj is not null
              and (
                  api.numeroCnj = :cnjLimpo
                  or replace(replace(replace(api.numeroCnj, '.', ''), '-', ''), '/', '') = :cnjLimpo
              )
            """)
    boolean existsByCnjAndIdNot(@Param("cnjLimpo") String cnjLimpo, @Param("id") Long id);
}
