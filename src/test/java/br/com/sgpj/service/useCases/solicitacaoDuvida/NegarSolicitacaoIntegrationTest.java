package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.MovimentacoesOficiais;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.MovimentacoesOficiaisRepository;
import br.com.sgpj.repository.ProcessoApiRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.cliente.AtualizarStatusClientePorProcessos;
import br.com.sgpj.service.useCases.processo.DeleteHistoricoProcesso;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import br.com.sgpj.service.useCases.processo.DeleteProcessoDocumento;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:negar-solicitacao;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import({NegarSolicitacao.class, DeleteProcessoApp.class, DeleteProcessoDocumento.class, DeleteHistoricoProcesso.class, AtualizarStatusClientePorProcessos.class})
class NegarSolicitacaoIntegrationTest {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private DocumentoRepository documentoRepository;
    @Autowired private HistoricoProcessoRepository historicoProcessoRepository;
    @Autowired private MovimentacoesOficiaisRepository movimentacoesOficiaisRepository;
    @Autowired private ProcessoApiRepository processoApiRepository;
    @Autowired private ProcessoAppRepository processoAppRepository;
    @Autowired private ProcessoDocumentoRepository processoDocumentoRepository;
    @Autowired private StatusClienteRepository statusClienteRepository;
    @Autowired private TipoAlteracaoRepository tipoAlteracaoRepository;
    @Autowired private TipoDocumentoRepository tipoDocumentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
        @Autowired private NegarSolicitacao negarSolicitacao;
        @MockitoSpyBean private DeleteProcessoApp deleteProcessoApp;

    @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void deveRemoverDependenciasDaSolicitacaoSemApagarDadosCompartilhadosOuDoCliente() {
        StatusCliente ativo = statusClienteRepository.save(new StatusCliente(null, "ATIVO"));
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial("Cliente preservado");
        cliente.setPF(true);
        cliente.setStatusCliente(ativo);
        LocalDateTime dataTransformacao = LocalDateTime.of(2025, 3, 1, 12, 0);
        cliente.setDataHoraTransformacaoEmCliente(dataTransformacao);
        cliente = clienteRepository.save(cliente);

        Contato contato = new Contato();
        contato.setCliente(cliente);
        contato.setEmail("cliente@exemplo.com");
        contato.setPrincipal(true);
        contato = contatoRepository.save(contato);

        Usuario usuario = new Usuario();
        usuario.setNome("Usuario de teste");
        usuario.setEmail("teste@exemplo.com");
        usuario = usuarioRepository.save(usuario);

        TipoAlteracao tipoAlteracao = tipoAlteracaoRepository.save(new TipoAlteracao(1L, "CRIACAO"));
        TipoDocumento tipoDocumento = tipoDocumentoRepository.save(new TipoDocumento(null, "Anexo", List.of()));
        Documento documento = new Documento();
        documento.setNomeOriginal("anexo.pdf");
        documento.setCriadoPorUsuario(usuario);
        documento.setTipoDocumento(tipoDocumento);
        documento = documentoRepository.save(documento);

        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00012345620268260004");
        processoApi = processoApiRepository.save(processoApi);
        MovimentacoesOficiais movimentacao = new MovimentacoesOficiais();
        movimentacao.setDescricao("Movimentacao preservada");
        movimentacao.setProcessoApi(processoApi);
        movimentacao = movimentacoesOficiaisRepository.save(movimentacao);

        ProcessoApp solicitacao = criarProcesso(cliente, null, StatusProcesso.SOLICITACAO);
        ProcessoApp outroProcesso = criarProcesso(cliente, null, StatusProcesso.ATIVO);
        solicitacao = processoAppRepository.save(solicitacao);
        outroProcesso = processoAppRepository.saveAndFlush(outroProcesso);
        solicitacao.setProcessoApi(processoApi);
        outroProcesso.setProcessoApi(processoApi);
        solicitacao = processoAppRepository.save(solicitacao);
        outroProcesso = processoAppRepository.saveAndFlush(outroProcesso);

        processoDocumentoRepository.save(new ProcessoDocumento(
                new ProcessoDocumentoId(documento.getId(), solicitacao.getId()), documento, solicitacao, usuario, LocalDateTime.now()));
        processoDocumentoRepository.save(new ProcessoDocumento(
                new ProcessoDocumentoId(documento.getId(), outroProcesso.getId()), documento, outroProcesso, usuario, LocalDateTime.now()));
        historicoProcessoRepository.save(new HistoricoProcesso(null, solicitacao, usuario,
                "Historico da solicitacao", LocalDateTime.now(), tipoAlteracao));
        historicoProcessoRepository.save(new HistoricoProcesso(null, outroProcesso, usuario,
                "Historico do outro processo", LocalDateTime.now(), tipoAlteracao));
        negarSolicitacao.negar(solicitacao.getId());

        assertFalse(processoAppRepository.existsById(solicitacao.getId()));
        assertTrue(processoAppRepository.existsById(outroProcesso.getId()));
        assertTrue(clienteRepository.existsById(cliente.getId()));
        assertEquals("ATIVO", clienteRepository.findById(cliente.getId()).orElseThrow().getStatusCliente().getNome());
        assertEquals(dataTransformacao, clienteRepository.findById(cliente.getId()).orElseThrow()
                .getDataHoraTransformacaoEmCliente());
        assertTrue(contatoRepository.existsById(contato.getId()));
        assertTrue(documentoRepository.existsById(documento.getId()));
        assertEquals(List.of(outroProcesso.getId()), processoDocumentoRepository.findByDocumentoId(documento.getId())
                .stream().map(vinculo -> vinculo.getProcessoApp().getId()).toList());
        assertEquals(1, historicoProcessoRepository.findByProcessoApp(
                processoAppRepository.findById(outroProcesso.getId()).orElseThrow()).size());
        assertTrue(processoApiRepository.existsById(processoApi.getId()));
        assertTrue(movimentacoesOficiaisRepository.existsById(movimentacao.getId()));
    }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void deveReverterAlteracaoDoClienteQuandoExclusaoFalhar() {
                StatusCliente lead = statusClienteRepository.save(new StatusCliente(null, "LEAD"));
                statusClienteRepository.save(new StatusCliente(null, "INATIVO"));
                Cliente cliente = new Cliente();
                cliente.setNomeRazaoSocial("Cliente rollback");
                cliente.setPF(true);
                cliente.setStatusCliente(lead);
                cliente = clienteRepository.save(cliente);

                ProcessoApp solicitacao = processoAppRepository.save(criarProcesso(cliente, null, StatusProcesso.SOLICITACAO));
                doThrow(new IllegalStateException("Falha simulada na exclusao"))
                                .when(deleteProcessoApp).deletarProcesso(anyLong());

                assertThrows(IllegalStateException.class, () -> negarSolicitacao.negar(solicitacao.getId()));

                assertTrue(processoAppRepository.existsById(solicitacao.getId()));
                assertEquals("LEAD", clienteRepository.findById(cliente.getId()).orElseThrow().getStatusCliente().getNome());
                assertNull(clienteRepository.findById(cliente.getId()).orElseThrow().getDataHoraTransformacaoEmCliente());
        }

    private ProcessoApp criarProcesso(Cliente cliente, ProcessoApi processoApi, StatusProcesso status) {
        ProcessoApp processo = new ProcessoApp();
        processo.setCliente(cliente);
        processo.setProcessoApi(processoApi);
        processo.setStatus(status);
        return processo;
    }
}