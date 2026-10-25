package br.com.sgpj.repository;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Autowired
    private ContatoRepository contatoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    @Test
    void deveFiltrarSolicitacoesComPesquisaStatusEstadoPeriodoEOrdenacaoSemDuplicarContatos() {
        StatusCliente leadStatus = statusClienteRepository.save(new StatusCliente(null, "LEAD"));
        Cliente maria = criarCliente("Maria Silva", leadStatus);
        Contato emailPrincipal = criarContato(maria, "maria@exemplo.com", true);
        criarContato(maria, "alternativo@exemplo.com", false);
        maria.setContatos(List.of(emailPrincipal));
        LocalDateTime dataEnvio = LocalDateTime.of(2026, 6, 15, 9, 30);
        ProcessoApp mariaProcesso = processoAppRepository.save(criarSolicitacao(maria, dataEnvio, "SP",
                "00012345620268260001"));
        processoAppRepository.save(criarSolicitacao(criarCliente("Outra pessoa", leadStatus),
                dataEnvio.minusDays(1), "RJ", null));
        processoAppRepository.flush();

        Page<ProcessoApp> porEmail = processoAppRepository.buscarSolicitacoesFiltradas(
                null, "Solicitação enviada pelo formulário público.", "ALTERNATIVO",
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0), PageRequest.of(0, 10,
                        Sort.by(Sort.Order.desc("dataHoraInicio"), Sort.Order.desc("id"))));
        Page<ProcessoApp> porNome = processoAppRepository.buscarSolicitacoesFiltradas(
                "SP", "Solicitação enviada pelo formulário público.", "mArIa",
                null, null, PageRequest.of(0, 10));
        Page<ProcessoApp> porCnj = processoAppRepository.buscarSolicitacoesFiltradas(
                null, "Solicitação enviada pelo formulário público.", "4562026",
                null, null, PageRequest.of(0, 10));
        Page<ProcessoApp> porEstado = processoAppRepository.buscarSolicitacoesFiltradas(
                "SP", "Solicitação enviada pelo formulário público.", null,
                null, null, PageRequest.of(0, 10));
        Page<ProcessoApp> porData = processoAppRepository.buscarSolicitacoesFiltradas(
                null, "Solicitação enviada pelo formulário público.", null,
                LocalDateTime.of(2026, 6, 15, 0, 0), LocalDateTime.of(2026, 6, 16, 0, 0),
                PageRequest.of(0, 10));
        Page<ProcessoApp> semFiltros = processoAppRepository.buscarSolicitacoesFiltradas(
                null, "Solicitação enviada pelo formulário público.", null,
                null, null, PageRequest.of(0, 1, Sort.by(Sort.Order.desc("dataHoraInicio"),
                        Sort.Order.desc("id"))));

        assertEquals(1, porEmail.getTotalElements());
        assertEquals(mariaProcesso.getId(), porEmail.getContent().getFirst().getId());
        assertEquals(1, porNome.getTotalElements());
        assertEquals(1, porCnj.getTotalElements());
        assertEquals(1, porEstado.getTotalElements());
        assertEquals(1, porData.getTotalElements());
        assertEquals(2, semFiltros.getTotalElements());
        assertEquals(1, semFiltros.getContent().size());
        assertEquals(mariaProcesso.getId(), semFiltros.getContent().getFirst().getId());
    }

    @Test
    void deveRetornarSomenteSolicitacoesPendentesEDevolverPaginaVazia() {
        StatusCliente leadStatus = statusClienteRepository.save(new StatusCliente(null, "LEAD"));
        StatusCliente ativoStatus = statusClienteRepository.save(new StatusCliente(null, "ATIVO"));
        StatusCliente inativoStatus = statusClienteRepository.save(new StatusCliente(null, "INATIVO"));
        processoAppRepository.save(criarSolicitacao(criarCliente("Lead", leadStatus), LocalDateTime.now(), "SP", null));
        processoAppRepository.save(criarSolicitacao(criarCliente("Ativo", ativoStatus), LocalDateTime.now(), "SP", null));
        processoAppRepository.save(criarSolicitacao(criarCliente("Inativo", inativoStatus), LocalDateTime.now(), "SP", null));
        processoAppRepository.flush();

        Page<ProcessoApp> pendentes = processoAppRepository.buscarSolicitacoesFiltradas(
                null, "Solicitação enviada pelo formulário público.", null,
                null, null, PageRequest.of(0, 10));
        Page<ProcessoApp> vazia = processoAppRepository.buscarSolicitacoesFiltradas(
                "ZZ", "Solicitação enviada pelo formulário público.", null,
                null, null, PageRequest.of(3, 5));

        assertEquals(1, pendentes.getTotalElements());
        assertEquals(0, vazia.getTotalElements());
        assertEquals(3, vazia.getNumber());
        assertEquals(5, vazia.getSize());
        assertEquals(0, vazia.getTotalPages());
    }

    @Test
    void deveDetectarOutrosProcessosDoClienteEDadosOficiaisCompartilhados() {
        StatusCliente leadStatus = statusClienteRepository.save(new StatusCliente(null, "LEAD"));
        Cliente cliente = criarCliente("Cliente compartilhado", leadStatus);
        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00012345620268260002");

        ProcessoApp processoSolicitacao = criarProcesso(cliente, LocalDateTime.now());
        processoSolicitacao.setProcessoApi(processoApi);
        processoSolicitacao = processoAppRepository.saveAndFlush(processoSolicitacao);

        ProcessoApp outroProcesso = criarProcesso(cliente, LocalDateTime.now());
        outroProcesso.setProcessoApi(processoApi);
        outroProcesso = processoAppRepository.saveAndFlush(outroProcesso);
        ProcessoApi processoApiExclusivo = new ProcessoApi();
        processoApiExclusivo.setNumeroCnj("00012345620268260003");
        ProcessoApp processoExclusivo = criarProcesso(cliente, LocalDateTime.now());
        processoExclusivo.setProcessoApi(processoApiExclusivo);
        processoExclusivo = processoAppRepository.saveAndFlush(processoExclusivo);

        assertTrue(processoAppRepository.existsByCliente_IdAndIdNot(cliente.getId(), processoSolicitacao.getId()));
        assertTrue(processoAppRepository.existsByProcessoApi_IdAndIdNot(
                processoApi.getId(), processoSolicitacao.getId()));
        assertFalse(processoAppRepository.existsByProcessoApi_IdAndIdNot(
                processoApiExclusivo.getId(), processoExclusivo.getId()));
    }

    @Test
    void deveBuscarAudienciasPorPeriodoFiltrandoStatusUsuarioAtivoEOrdenandoPorData() {
        StatusCliente ativoStatus = statusClienteRepository.save(new StatusCliente(null, "ATIVO"));
        Cliente cliente = criarCliente("Cliente Audiencia", ativoStatus);

        Usuario usuarioAtivo = new Usuario("Advogado Ativo", "ativo@sgpj.com.br", "senha", null, true);
        usuarioAtivo.setCriadoEm(LocalDateTime.now());
        usuarioAtivo = usuarioRepository.save(usuarioAtivo);

        Usuario usuarioInativo = new Usuario("Advogado Inativo", "inativo@sgpj.com.br", "senha", null, false);
        usuarioInativo.setCriadoEm(LocalDateTime.now());
        usuarioInativo = usuarioRepository.save(usuarioInativo);

        LocalDateTime inicioPeriodo = LocalDateTime.of(2026, 10, 25, 0, 0);
        LocalDateTime fimPeriodo = LocalDateTime.of(2026, 11, 1, 0, 0);

        ProcessoApp audienciaMaisTarde = criarProcessoComAudiencia(
                cliente, usuarioAtivo, StatusProcesso.ATIVO, LocalDateTime.of(2026, 10, 28, 15, 0));
        ProcessoApp audienciaMaisCedo = criarProcessoComAudiencia(
                cliente, usuarioAtivo, StatusProcesso.ATIVO, LocalDateTime.of(2026, 10, 26, 9, 30));
        // Fora do período
        criarProcessoComAudiencia(
                cliente, usuarioAtivo, StatusProcesso.ATIVO, LocalDateTime.of(2026, 11, 1, 0, 0));
        // Processo suspenso
        criarProcessoComAudiencia(
                cliente, usuarioAtivo, StatusProcesso.SUSPENSO, LocalDateTime.of(2026, 10, 27, 10, 0));
        // Usuário inativo
        criarProcessoComAudiencia(
                cliente, usuarioInativo, StatusProcesso.ATIVO, LocalDateTime.of(2026, 10, 27, 11, 0));
        processoAppRepository.flush();

        List<ProcessoApp> resultado = processoAppRepository.buscarAudienciasPorPeriodo(
                StatusProcesso.ATIVO, inicioPeriodo, fimPeriodo);

        assertEquals(2, resultado.size());
        assertEquals(audienciaMaisCedo.getId(), resultado.get(0).getId());
        assertEquals(audienciaMaisTarde.getId(), resultado.get(1).getId());
    }

    private ProcessoApp criarProcessoComAudiencia(
            Cliente cliente,
            Usuario usuarioResponsavel,
            StatusProcesso status,
            LocalDateTime dataAudiencia
    ) {
        ProcessoApp processo = new ProcessoApp();
        processo.setCliente(cliente);
        processo.setUsuarioResponsavel(usuarioResponsavel);
        processo.setStatus(status);
        processo.setDataAudiencia(dataAudiencia);
        processo.setDataHoraInicio(LocalDateTime.now());
        return processoAppRepository.save(processo);
    }

    private Contato criarContato(Cliente cliente, String email, boolean principal) {
        Contato contato = new Contato();
        contato.setCliente(cliente);
        contato.setEmail(email);
        contato.setPrincipal(principal);
        return contatoRepository.save(contato);
    }

    private ProcessoApp criarSolicitacao(Cliente cliente, LocalDateTime dataHoraInicio, String estado, String cnj) {
        ProcessoApp processo = criarProcesso(cliente, dataHoraInicio);
        processo.setObservacao("Solicitação enviada pelo formulário público. Estado: " + estado);
        if (cnj != null) {
            ProcessoApi processoApi = new ProcessoApi();
            processoApi.setNumeroCnj(cnj);
            processo.setProcessoApi(processoApi);
        }
        return processo;
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
