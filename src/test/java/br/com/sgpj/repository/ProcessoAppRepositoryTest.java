package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:processo-app-repository;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class ProcessoAppRepositoryTest {

    @Autowired
    private ProcessoAppRepository processoAppRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private StatusClienteRepository statusClienteRepository;

    @Test
    void devePaginarLeadsOrdenandoPorDataEIdEContarSomenteLeads() {
        StatusCliente leadStatus = statusClienteRepository.save(new StatusCliente(null, "LEAD"));
        StatusCliente ativoStatus = statusClienteRepository.save(new StatusCliente(null, "ATIVO"));
        Cliente lead1 = criarCliente("Lead um", leadStatus);
        Cliente lead2 = criarCliente("Lead dois", leadStatus);
        Cliente lead3 = criarCliente("Lead tres", leadStatus);
        Cliente ativo = criarCliente("Cliente ativo", ativoStatus);
        LocalDateTime maisRecente = LocalDateTime.of(2026, 10, 3, 10, 0);

        processoAppRepository.save(criarProcesso(lead1, maisRecente));
        ProcessoApp leadMaisRecente = processoAppRepository.save(criarProcesso(lead2, maisRecente));
        processoAppRepository.save(criarProcesso(lead3, maisRecente.minusDays(1)));
        processoAppRepository.save(criarProcesso(ativo, maisRecente.plusDays(1)));
        processoAppRepository.flush();

        Sort sort = Sort.by(Sort.Order.desc("dataHoraInicio"), Sort.Order.desc("id"));
        Page<ProcessoApp> primeiraPagina = processoAppRepository
                .findByCliente_StatusCliente_NomeIgnoreCase("lead", PageRequest.of(0, 1, sort));
        Page<ProcessoApp> segundaPagina = processoAppRepository
                .findByCliente_StatusCliente_NomeIgnoreCase("LEAD", PageRequest.of(1, 1, sort));

        assertEquals(1, primeiraPagina.getContent().size());
        assertEquals(leadMaisRecente.getId(), primeiraPagina.getContent().getFirst().getId());
        assertEquals(3, primeiraPagina.getTotalElements());
        assertEquals(3, primeiraPagina.getTotalPages());
        assertEquals(1, segundaPagina.getContent().size());
        assertEquals(1, segundaPagina.getNumber());
        assertEquals(3, segundaPagina.getTotalElements());
    }

    private Cliente criarCliente(String nome, StatusCliente status) {
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(nome);
        cliente.setPF(true);
        cliente.setStatusCliente(status);
        return clienteRepository.save(cliente);
    }

    private ProcessoApp criarProcesso(Cliente cliente, LocalDateTime dataHoraInicio) {
        ProcessoApp processo = new ProcessoApp();
        processo.setCliente(cliente);
        processo.setDataHoraInicio(dataHoraInicio);
        processo.setStatus(StatusProcesso.SOLICITACAO);
        return processo;
    }
}
