package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.PrioridadeRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import br.com.sgpj.service.useCases.onedrive.GarantirPastasClienteOneDrive;
import br.com.sgpj.service.useCases.onedrive.RegistrarCaminhoArmazenamentoCliente;
import br.com.sgpj.service.useCases.onedrive.OneDriveGraphGateway;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:aceitar-solicitacao;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import({AceitarSolicitacao.class, RegistroHistoricoProcesso.class, GarantirPastasClienteOneDrive.class,
        RegistrarCaminhoArmazenamentoCliente.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AceitarSolicitacaoIntegrationTest {

    @Autowired private AceitarSolicitacao aceitarSolicitacao;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private FaseProcessoRepository faseProcessoRepository;
    @Autowired private HistoricoProcessoRepository historicoProcessoRepository;
    @Autowired private PrioridadeRepository prioridadeRepository;
    @Autowired private ProcessoAppRepository processoAppRepository;
    @Autowired private StatusClienteRepository statusClienteRepository;
    @Autowired private TipoAlteracaoRepository tipoAlteracaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @MockitoBean private OneDriveGraphGateway graphGateway;

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
        List<HistoricoProcesso> historicos = historicoProcessoRepository.findByProcessoApp(solicitacaoPersistida);
        assertEquals(1, historicos.size());
        assertEquals(usuarioResponsavel.getId(), historicos.getFirst().getUsuarioUtilizado().getId());
        assertEquals("ABERTURA DE PROCESSO", historicos.getFirst().getTipoAlteracao().getNome());
        assertEquals("Processo criado.", historicos.getFirst().getDescricaoAlteracao());
        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", "cliente-preservado");
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
        verifyNoInteractions(graphGateway);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveReverterAceiteQuandoNaoEncontrarTipoDeHistorico() {
        prepararReferencias();
        Usuario usuarioResponsavel = criarUsuarioResponsavel();
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Cliente sem tipo de histórico");
        cliente.setPF(true);
        cliente.setStatusCliente(status("LEAD"));
        cliente = clienteRepository.save(cliente);
        ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.SOLICITACAO));
        tipoAlteracaoRepository.deleteAll();

        assertThrows(ObjetoNaoEncontradoException.class,
                () -> aceitarSolicitacao.aceitar(solicitacao.getId(), requestPf("Contato"), usuarioResponsavel));

        assertEquals(StatusProcesso.SOLICITACAO,
                processoAppRepository.findById(solicitacao.getId()).orElseThrow().getStatus());
        assertEquals("LEAD", clienteRepository.findById(cliente.getId()).orElseThrow().getStatusCliente().getNome());
        assertEquals(0, historicoProcessoRepository.count());
        verifyNoInteractions(graphGateway);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveAceitarEPreservarCaminhoSalvoQuandoOneDriveFalhar() {
        prepararReferencias();
        status("ATIVO");
        Usuario usuario = criarUsuarioResponsavel();
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Nome atual " + UUID.randomUUID());
        cliente.setPF(true);
        cliente.setStatusCliente(status("LEAD"));
        cliente = clienteRepository.save(cliente);
        String caminho = "nome-anterior-" + UUID.randomUUID();
        caminhoRepository.save(new CaminhoPadraoDeArmazenamento(null, cliente, caminho, List.of()));
        ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.SOLICITACAO));
        when(graphGateway.garantirPasta("root:/sgpj/usuarios:", caminho))
                .thenThrow(new OneDriveErroException("Sem conexao"));

        aceitarSolicitacao.aceitar(solicitacao.getId(), requestPf("Contato"), usuario);

        assertEquals(StatusProcesso.ATIVO, processoAppRepository.findById(solicitacao.getId()).orElseThrow().getStatus());
        assertEquals("ATIVO", clienteRepository.findById(cliente.getId()).orElseThrow().getStatusCliente().getNome());
        assertEquals(caminho, caminhoRepository.findByClienteId(cliente.getId()).getFirst().getCaminho());
        assertEquals(1, caminhoRepository.findByClienteId(cliente.getId()).size());
        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", caminho);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveCadastrarCaminhoAusenteEAceitarMesmoSemOneDrive() {
        prepararReferencias();
        status("ATIVO");
        Usuario usuario = criarUsuarioResponsavel();
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("cliente-antigo-" + UUID.randomUUID());
        cliente.setPF(true);
        cliente.setStatusCliente(status("LEAD"));
        cliente = clienteRepository.save(cliente);
        ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, StatusProcesso.SOLICITACAO));
        String caminho = cliente.getNomeRazaoSocial();
        when(graphGateway.garantirPasta("root:/sgpj/usuarios:", caminho))
                .thenThrow(new OneDriveErroException("Sem conexao"));

        aceitarSolicitacao.aceitar(solicitacao.getId(), requestPf("Contato"), usuario);

        assertEquals(StatusProcesso.ATIVO, processoAppRepository.findById(solicitacao.getId()).orElseThrow().getStatus());
        var caminhos = caminhoRepository.findByClienteId(cliente.getId());
        assertEquals(1, caminhos.size());
        assertEquals(caminho, caminhos.getFirst().getCaminho());
        verify(graphGateway).garantirPasta("root:/sgpj/usuarios:", caminho);
    }

    private void prepararReferencias() {
        if (prioridadeProcesso("MEDIA").isEmpty()) prioridadeRepository.save(new Prioridade(2L, "MEDIA"));
        if (fase("INICIAL").isEmpty()) faseProcessoRepository.save(new FaseProcesso(1L, "INICIAL"));
        if (tipoAlteracaoRepository.findByNomeIgnoreCase(RegistroHistoricoProcesso.ABERTURA).isEmpty()) {
            tipoAlteracaoRepository.save(new TipoAlteracao(1L, RegistroHistoricoProcesso.ABERTURA));
        }
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