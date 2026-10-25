package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:aceitar-solicitacao;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import(AceitarSolicitacao.class)
class AceitarSolicitacaoIntegrationTest {

    @Autowired private AceitarSolicitacao aceitarSolicitacao;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private FaseProcessoRepository faseProcessoRepository;
    @Autowired private PrioridadeRepository prioridadeRepository;
    @Autowired private ProcessoAppRepository processoAppRepository;
    @Autowired private StatusClienteRepository statusClienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveCompletarClienteEAceitarSolicitacaoPreservandoOutroProcesso() {
        prepararReferencias();
        Usuario usuarioResponsavel = criarUsuarioResponsavel();
        StatusCliente ativo = status("ATIVO");
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Cliente preservado");
        cliente.setPF(true);
        cliente.setStatusCliente(ativo);
        LocalDateTime transformacao = LocalDateTime.of(2025, 3, 1, 12, 0);
        cliente.setDataHoraTransformacaoEmCliente(transformacao);
        cliente = clienteRepository.save(cliente);

        ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.SOLICITACAO));
        ProcessoApp outroProcesso = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.ATIVO));

        aceitarSolicitacao.aceitar(solicitacao.getId(), requestPf("Contato incluído"), usuarioResponsavel);

        Cliente clientePersistido = clienteRepository.findById(cliente.getId()).orElseThrow();
        ProcessoApp solicitacaoPersistida = processoAppRepository.findById(solicitacao.getId()).orElseThrow();
        ProcessoApp outroProcessoPersistido = processoAppRepository.findById(outroProcesso.getId()).orElseThrow();

        assertEquals("Cliente preservado", clientePersistido.getNomeRazaoSocial());
        assertEquals(ativo.getId(), clientePersistido.getStatusCliente().getId());
        assertEquals(transformacao, clientePersistido.getDataHoraTransformacaoEmCliente());
        assertEquals("01001000", clientePersistido.getEndereco().getCep());
        assertEquals(1, contatoRepository.findAll().size());
        assertEquals("Contato incluído", contatoRepository.findAll().getFirst().getNome());
        assertEquals(StatusProcesso.ATIVO, solicitacaoPersistida.getStatus());
        assertEquals("MEDIA", solicitacaoPersistida.getPrioridade().getNome());
        assertEquals("INICIAL", solicitacaoPersistida.getFaseProcesso().getNome());
        assertEquals(usuarioResponsavel.getId(), solicitacaoPersistida.getUsuarioResponsavel().getId());
        assertEquals(StatusProcesso.ATIVO, outroProcessoPersistido.getStatus());
        assertEquals(cliente.getId(), outroProcessoPersistido.getCliente().getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveReverterTodasAsAlteracoesQuandoFalharAoPersistirContato() {
        prepararReferencias();
        Usuario usuarioResponsavel = criarUsuarioResponsavel();
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Cliente rollback");
        cliente.setPF(true);
        cliente.setStatusCliente(status("LEAD"));
        cliente = clienteRepository.save(cliente);
        ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.SOLICITACAO));
        String nomeExcessivo = "x".repeat(151);

        assertThrows(RuntimeException.class,
                () -> aceitarSolicitacao.aceitar(solicitacao.getId(), requestPf(nomeExcessivo), usuarioResponsavel));

        Cliente clientePersistido = clienteRepository.findById(cliente.getId()).orElseThrow();
        ProcessoApp solicitacaoPersistida = processoAppRepository.findById(solicitacao.getId()).orElseThrow();
        assertEquals("LEAD", clientePersistido.getStatusCliente().getNome());
        assertNull(clientePersistido.getDataHoraTransformacaoEmCliente());
        assertNull(clientePersistido.getEndereco());
        assertEquals(0, contatoRepository.count());
        assertEquals(StatusProcesso.SOLICITACAO, solicitacaoPersistida.getStatus());
        assertEquals(0, enderecoRepository.count());
    }

    private void prepararReferencias() {
        if (prioridadeProcesso("MEDIA").isEmpty()) prioridadeRepository.save(new Prioridade(2L, "MEDIA"));
        if (fase("INICIAL").isEmpty()) faseProcessoRepository.save(new FaseProcesso(1L, "INICIAL"));
    }

    private Usuario criarUsuarioResponsavel() {
        return usuarioRepository.save(new Usuario(
                "Responsável", "responsavel-" + UUID.randomUUID() + "@example.com", "senha", null, true));
    }

    private Optional<Prioridade> prioridadeProcesso(String nome) {
        return prioridadeRepository.findByNomeIgnoreCase(nome);
    }

    private Optional<FaseProcesso> fase(String nome) {
        return faseProcessoRepository.findByNomeIgnoreCase(nome);
    }

    private StatusCliente status(String nome) {
        return statusClienteRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> statusClienteRepository.save(new StatusCliente(null, nome)));
    }

    private ProcessoApp criarProcesso(Cliente cliente, StatusProcesso status) {
        ProcessoApp processo = new ProcessoApp();
        processo.setCliente(cliente);
        processo.setStatus(status);
        return processo;
    }

    private SolicitacaoAceiteRequest requestPf(String nomeContato) {
        return new SolicitacaoAceiteRequest(
                new SolicitacaoAceiteRequest.Client(
                        "PF", "Nome enviado", null, "contato@example.com", "11999999999",
                        "52998224725", null, null, "Advogada",
                        new SolicitacaoAceiteRequest.Address("01001000", "Praça da Sé", "100", null,
                                "Sé", "São Paulo", "SP"),
                        List.of(new SolicitacaoAceiteRequest.Contact(nomeContato, "contato@example.com",
                                "11999999999", "Advogada", true))),
                new SolicitacaoAceiteRequest.Process(null, "média", "Inicial", null, null, null));
    }
}