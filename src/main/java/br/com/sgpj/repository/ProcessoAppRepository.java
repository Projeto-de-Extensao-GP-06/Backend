package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessoAppRepository extends JpaRepository<ProcessoApp, Long> {
    List<ProcessoApp> findByCliente(Cliente cliente);

    List<ProcessoApp> findByClienteNomeRazaoSocialContainingIgnoreCaseOrProcessoApiNumeroCnjContainingIgnoreCaseAndFaseProcessoOrSiglaTribunal(String query, String query1, FaseProcesso faseProcesso, SiglaTribunal siglaTribunal);

    List<ProcessoApp> findByClienteNomeRazaoSocialContainingIgnoreCaseOrProcessoApiNumeroCnjContainingIgnoreCase(String query, String query1);
}
