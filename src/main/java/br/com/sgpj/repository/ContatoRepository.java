package br.com.sgpj.repository;

import br.com.sgpj.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ContatoRepository extends JpaRepository<Contato, Long> {

    @Query("SELECT c FROM Contato c JOIN FETCH c.cliente cli LEFT JOIN FETCH cli.statusCliente WHERE LOWER(TRIM(c.email)) = LOWER(TRIM(:email)) AND cli.id <> :clienteIdSolicitacao")
    List<Contato> findContatosPorEmail(@Param("email") String email, @Param("clienteIdSolicitacao") Long clienteIdSolicitacao);

    @Query("SELECT c FROM Contato c JOIN FETCH c.cliente cli LEFT JOIN FETCH cli.statusCliente " +
           "WHERE replace(replace(replace(replace(replace(c.telefone, '(', ''), ')', ''), '-', ''), ' ', ''), '+', '') = :telefoneDigitos " +
           "AND cli.id <> :clienteIdSolicitacao")
    List<Contato> findContatosPorTelefoneNormalizado(@Param("telefoneDigitos") String telefoneDigitos, @Param("clienteIdSolicitacao") Long clienteIdSolicitacao);
}

