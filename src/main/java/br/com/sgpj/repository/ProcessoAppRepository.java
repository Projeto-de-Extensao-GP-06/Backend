package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long> {
    List<ProcessoApp> findByCliente(Cliente cliente);

    List<ProcessoApp> findByClienteNomeRazaoSocialContainingIgnoreCaseOrProcessoApiNumeroCnjContainingIgnoreCase(String query, String query1);
}
