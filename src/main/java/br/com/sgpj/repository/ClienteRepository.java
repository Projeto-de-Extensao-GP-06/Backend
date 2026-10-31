package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    List<Cliente> findByStatusCliente_NomeIgnoreCase(String statusNome);

    List<Cliente> findByStatusCliente_NomeNotIgnoreCase(String statusNome);

    List<Cliente> findByNomeRazaoSocialContainingIgnoreCaseOrCpfCnpjContaining(String nome, String cpfCnpj);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.contatos LEFT JOIN FETCH c.endereco LEFT JOIN FETCH c.statusCliente WHERE c.id = :id")
    Optional<Cliente> findByIdComDetalhes(Long id);

    boolean existsByCpfCnpj(String cpfCnpj);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.statusCliente LEFT JOIN FETCH c.contatos WHERE c.cpfCnpj = :cpfCnpj AND c.id <> :clienteIdSolicitacao")
    List<Cliente> findCandidatosPorCpfCnpj(@org.springframework.data.repository.query.Param("cpfCnpj") String cpfCnpj,
                                           @org.springframework.data.repository.query.Param("clienteIdSolicitacao") Long clienteIdSolicitacao);

    @Query("SELECT c FROM Cliente c LEFT JOIN FETCH c.statusCliente LEFT JOIN FETCH c.contatos WHERE LOWER(TRIM(c.nomeRazaoSocial)) = LOWER(TRIM(:nome)) AND c.id <> :clienteIdSolicitacao")
    List<Cliente> findCandidatosPorNome(@org.springframework.data.repository.query.Param("nome") String nome,
                                        @org.springframework.data.repository.query.Param("clienteIdSolicitacao") Long clienteIdSolicitacao);
}