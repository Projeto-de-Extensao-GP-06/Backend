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

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long>, JpaSpecificationExecutor<ProcessoApp> {
    List<ProcessoApp> findByCliente(Cliente cliente);

    List<ProcessoApp> findByClienteId(Long clienteId);

      boolean existsByCliente_IdAndIdNot(Long clienteId, Long processoId);

      boolean existsByProcessoApi_IdAndIdNot(Long processoApiId, Long processoId);

    Page<ProcessoApp> findByCliente_StatusCliente_NomeIgnoreCase(String statusNome, Pageable pageable);

    @Query(value = """
            select distinct p
            from ProcessoApp p
            left join p.cliente c
            left join c.statusCliente statusCliente
            left join p.processoApi api
            where lower(p.observacao) like lower(concat(:estadoPrefixo, '%'))
              and (:estado is null or lower(p.observacao) = lower(concat(:estadoPrefixo, ' Estado: ', :estado)))
              and (:pesquisa is null
                   or lower(c.nomeRazaoSocial) like lower(concat('%', :pesquisa, '%'))
                   or exists (
                       select contato.id from Contato contato
                       where contato.cliente = c
                         and lower(contato.email) like lower(concat('%', :pesquisa, '%'))
                   )
                   or lower(api.numeroCnj) like lower(concat('%', :pesquisa, '%')))
              and (statusCliente.nome is null or upper(statusCliente.nome) not in ('ATIVO', 'INATIVO'))
              and (:dataInicio is null or p.dataHoraInicio >= :dataInicio)
              and (:dataFim is null or p.dataHoraInicio < :dataFim)
            """,
            countQuery = """
            select count(distinct p.id)
            from ProcessoApp p
            left join p.cliente c
            left join c.statusCliente statusCliente
            left join p.processoApi api
            where lower(p.observacao) like lower(concat(:estadoPrefixo, '%'))
              and (:estado is null or lower(p.observacao) = lower(concat(:estadoPrefixo, ' Estado: ', :estado)))
              and (:pesquisa is null
                   or lower(c.nomeRazaoSocial) like lower(concat('%', :pesquisa, '%'))
                   or exists (
                       select contato.id from Contato contato
                       where contato.cliente = c
                         and lower(contato.email) like lower(concat('%', :pesquisa, '%'))
                   )
                   or lower(api.numeroCnj) like lower(concat('%', :pesquisa, '%')))
              and (statusCliente.nome is null or upper(statusCliente.nome) not in ('ATIVO', 'INATIVO'))
              and (:dataInicio is null or p.dataHoraInicio >= :dataInicio)
              and (:dataFim is null or p.dataHoraInicio < :dataFim)
            """)
    Page<ProcessoApp> buscarSolicitacoesFiltradas(
            @Param("estado") String estado,
            @Param("estadoPrefixo") String estadoPrefixo,
            @Param("pesquisa") String pesquisa,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable);

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
