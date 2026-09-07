package br.com.sgpj.specification;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.service.useCases.cliente.ClienteSpecification;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteSpecification")
class ClienteSpecificationTest {

    @Mock
    private Root<Cliente> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<Object> statusClientePath;

    @Mock
    private Path<Object> nomeStatusPath;

    @Mock
    private Path<Object> nomeRazaoSocialPath;

    @Mock
    private Path<String> cpfCnpjPath;

    @Mock
    private Path<Object> emailPath;

    @Mock
    private Join<Object, Object> contatosJoin;

    @Mock
    private Expression<String> lowerExpression;

    @Mock
    private Expression<Integer> lengthExpression;

    @Mock
    private Predicate predicate;

    @Nested
    @DisplayName("Filtro por Status")
    class StatusFiltro {

        @Test
        @DisplayName("Deve excluir status LEAD quando status for 'Todos'")
        void deveExcluirStatusLeadQuandoStatusForTodos() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Todos", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).notEqual(any(Expression.class), eq("lead"));
            verify(cb, never()).equal(any(Expression.class), eq("lead"));
        }

        @Test
        @DisplayName("Deve excluir status LEAD quando status for nulo")
        void deveExcluirStatusLeadQuandoStatusForNulo() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, null, "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).notEqual(any(Expression.class), eq("lead"));
        }

        @Test
        @DisplayName("Deve excluir status LEAD quando status for vazio/espaços")
        void deveExcluirStatusLeadQuandoStatusForVazio() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "   ", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).notEqual(any(Expression.class), eq("lead"));
        }

        @Test
        @DisplayName("Deve filtrar por status específico quando status não for 'Todos'")
        void deveFiltrarPorStatusEspecificoQuandoStatusNaoForTodos() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).equal(any(Expression.class), eq("ativo"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Ativo", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).equal(any(Expression.class), eq("ativo"));
            verify(cb, never()).notEqual(any(Expression.class), eq("lead"));
        }

        @Test
        @DisplayName("Deve permitir buscar status LEAD se especificamente solicitado")
        void devePermitirBuscarStatusLeadEspecifico() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).equal(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "LEAD", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).equal(any(Expression.class), eq("lead"));
            verify(cb, never()).notEqual(any(Expression.class), eq("lead"));
        }
    }

    @Nested
    @DisplayName("Filtro por Termo de Busca")
    class TermoFiltro {

        @Test
        @DisplayName("Deve adicionar filtros por nome, CPF/CNPJ e e-mail quando termo for fornecido")
        void deveAdicionarFiltrosDeTermoQuandoTermoInformado() {
            doReturn(nomeRazaoSocialPath).when(root).get("nomeRazaoSocial");
            doReturn(cpfCnpjPath).when(root).get("cpfCnpj");
            doReturn(contatosJoin).when(root).join("contatos", JoinType.LEFT);
            doReturn(emailPath).when(contatosJoin).get("email");

            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).like(any(Expression.class), anyString());
            doReturn(predicate).when(cb).or(any(), any(), any());
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(" João ", "Todos", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb, times(3)).like(any(Expression.class), eq("%joão%"));
            verify(cb).or(any(), any(), any());
            verify(query).distinct(true);
        }

        @Test
        @DisplayName("Não deve adicionar filtro de termo se termo for nulo")
        void naoDeveAdicionarFiltroSeTermoForNulo() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Todos", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(query, never()).distinct(true);
            verify(cb, never()).like(any(Expression.class), anyString());
        }

        @Test
        @DisplayName("Não deve adicionar filtro de termo se termo for vazio/espaços")
        void naoDeveAdicionarFiltroSeTermoForVazio() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros("   ", "Todos", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(query, never()).distinct(true);
            verify(cb, never()).like(any(Expression.class), anyString());
        }
    }

    @Nested
    @DisplayName("Filtro por Tipo de Pessoa")
    class TipoPessoaFiltro {

        @Test
        @DisplayName("Deve filtrar por tamanho do CPF (11 caracteres) quando tipoPessoa for 'PF'")
        void deveFiltrarPorCpfParaPessoaFisica() {
            doReturn(cpfCnpjPath).when(root).get("cpfCnpj");
            doReturn(lengthExpression).when(cb).length(cpfCnpjPath);
            doReturn(predicate).when(cb).equal(lengthExpression, 11);
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Todos", "PF");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).equal(lengthExpression, 11);
        }

        @Test
        @DisplayName("Deve filtrar por tamanho do CNPJ (14 caracteres) quando tipoPessoa for 'PJ'")
        void deveFiltrarPorCnpjParaPessoaJuridica() {
            doReturn(cpfCnpjPath).when(root).get("cpfCnpj");
            doReturn(lengthExpression).when(cb).length(cpfCnpjPath);
            doReturn(predicate).when(cb).equal(lengthExpression, 14);
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Todos", "PJ");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb).equal(lengthExpression, 14);
        }

        @Test
        @DisplayName("Não deve adicionar filtro por tipo de pessoa se for 'Todos'")
        void naoDeveAdicionarFiltroSeTipoPessoaForTodos() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros(null, "Todos", "Todos");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(cb, never()).length(any());
        }

        @Test
        @DisplayName("Não deve adicionar filtro por tipo de pessoa se for nulo ou invalido")
        void naoDeveAdicionarFiltroSeTipoPessoaForNuloOuInvalido() {
            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");
            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(predicate).when(cb).notEqual(any(Expression.class), eq("lead"));
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec1 = ClienteSpecification.comFiltros(null, "Todos", null);
            spec1.toPredicate(root, query, cb);

            Specification<Cliente> spec2 = ClienteSpecification.comFiltros(null, "Todos", "Outro");
            spec2.toPredicate(root, query, cb);

            verify(cb, never()).length(any());
        }
    }

    @Nested
    @DisplayName("Filtros Combinados")
    class FiltrosCombinados {

        @Test
        @DisplayName("Deve aplicar todos os filtros simultaneamente quando todos forem preenchidos")
        void deveAplicarTodosOsFiltrosSimultaneamente() {
            doReturn(nomeRazaoSocialPath).when(root).get("nomeRazaoSocial");
            doReturn(cpfCnpjPath).when(root).get("cpfCnpj");
            doReturn(contatosJoin).when(root).join("contatos", JoinType.LEFT);
            doReturn(emailPath).when(contatosJoin).get("email");

            doReturn(statusClientePath).when(root).get("statusCliente");
            doReturn(nomeStatusPath).when(statusClientePath).get("nome");

            doReturn(lowerExpression).when(cb).lower(any());
            doReturn(lengthExpression).when(cb).length(cpfCnpjPath);
            doReturn(predicate).when(cb).like(any(Expression.class), anyString());
            doReturn(predicate).when(cb).or(any(), any(), any());
            doReturn(predicate).when(cb).equal(any(Expression.class), eq("ativo"));
            doReturn(predicate).when(cb).equal(lengthExpression, 11);
            doReturn(predicate).when(cb).and(any(Predicate[].class));

            Specification<Cliente> spec = ClienteSpecification.comFiltros("teste", "Ativo", "PF");
            Predicate result = spec.toPredicate(root, query, cb);

            assertNotNull(result);
            verify(query).distinct(true);
            verify(cb).equal(any(Expression.class), eq("ativo"));
            verify(cb).equal(lengthExpression, 11);
        }
    }
}
