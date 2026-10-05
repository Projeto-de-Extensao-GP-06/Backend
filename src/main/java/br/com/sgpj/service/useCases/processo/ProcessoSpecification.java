package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProcessoSpecification {

    private ProcessoSpecification() {}

    public static Specification<ProcessoApp> comFiltros(
            String query,
            String status,
            Long faseId,
            Long tribunalId,
            Long prioridadeId,
            String audiencia
    ) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclui sempre SOLICITACAO (e NEGADO) da listagem principal
            predicates.add(cb.notEqual(root.get("status"), StatusProcesso.SOLICITACAO));

            // Filtro de busca por texto: nome do cliente ou número CNJ
            if (query != null && !query.isBlank()) {
                String termo = "%" + query.trim().toLowerCase() + "%";
                var clienteJoin = root.join("cliente", JoinType.LEFT);
                var apiJoin = root.join("processoApi", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(clienteJoin.get("nomeRazaoSocial")), termo),
                        cb.like(cb.lower(apiJoin.get("numeroCnj")), termo)
                ));
                criteriaQuery.distinct(true);
            }

            // Filtro por status (enum)
            if (status != null && !status.isBlank() && !status.equalsIgnoreCase("Todos")) {
                try {
                    StatusProcesso statusEnum = StatusProcesso.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // status inválido → ignora o filtro
                }
            }

            // Filtro por fase
            if (faseId != null) {
                predicates.add(cb.equal(root.get("faseProcesso").get("id"), faseId));
            }

            // Filtro por tribunal
            if (tribunalId != null) {
                predicates.add(cb.equal(root.get("siglaTribunal").get("id"), tribunalId));
            }

            // Filtro por prioridade
            if (prioridadeId != null) {
                predicates.add(cb.equal(root.get("prioridade").get("id"), prioridadeId));
            }

            // Filtro por audiência
            if ("com".equalsIgnoreCase(audiencia)) {
                predicates.add(cb.isNotNull(root.get("dataAudiencia")));
            } else if ("sem".equalsIgnoreCase(audiencia)) {
                predicates.add(cb.isNull(root.get("dataAudiencia")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
