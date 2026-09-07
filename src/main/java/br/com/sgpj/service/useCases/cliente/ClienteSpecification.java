package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Cliente;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ClienteSpecification {

    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CNPJ = 14;

    private ClienteSpecification() {}

    public static Specification<Cliente> comFiltros(String termo, String status, String tipoPessoa) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (termo != null && !termo.isBlank()) {
                String termoFormatado = "%" + termo.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nomeRazaoSocial")), termoFormatado),
                        cb.like(cb.lower(root.get("cpfCnpj")), termoFormatado),
                        cb.like(cb.lower(root.join("contatos", JoinType.LEFT).get("email")), termoFormatado)
                ));
                query.distinct(true);
            }

            if (status != null && !status.isBlank() && !status.equalsIgnoreCase("Todos")) {
                predicates.add(cb.equal(cb.lower(root.get("statusCliente").get("nome")), status.trim().toLowerCase()));
            } else {
                predicates.add(cb.notEqual(cb.lower(root.get("statusCliente").get("nome")), "lead"));
            }

            if (tipoPessoa != null && !tipoPessoa.isBlank() && !tipoPessoa.equalsIgnoreCase("Todos")) {
                if (tipoPessoa.equalsIgnoreCase("PF")) {
                    predicates.add(cb.equal(cb.length(root.get("cpfCnpj")), TAMANHO_CPF));
                } else if (tipoPessoa.equalsIgnoreCase("PJ")) {
                    predicates.add(cb.equal(cb.length(root.get("cpfCnpj")), TAMANHO_CNPJ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
