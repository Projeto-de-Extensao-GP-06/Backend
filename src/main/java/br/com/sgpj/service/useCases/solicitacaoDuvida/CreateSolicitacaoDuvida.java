package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ContatoMapper;
import br.com.sgpj.mapper.ProcessoAppToSolicitacaoDuvidaMapper;
import br.com.sgpj.mapper.SolicitacaoDuvidaMapper;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ContatoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.onedrive.ClienteCaminhoArmazenamento;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateSolicitacaoDuvida {

    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público.";

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final ClienteCaminhoArmazenamento clienteCaminhoArmazenamento;
    private final PrepararVinculoProcessoOficial prepararVinculoProcessoOficial;
    private final SincronizacaoProcessoProducer sincronizacaoProducer;

    public CreateSolicitacaoDuvida(ClienteRepository clienteRepository,
                                    ContatoRepository contatoRepository,
                                    ProcessoAppRepository processoAppRepository,
                                    StatusClienteRepository statusClienteRepository,
                                    PrepararVinculoProcessoOficial prepararVinculoProcessoOficial,
                                    SincronizacaoProcessoProducer sincronizacaoProducer,
                                    ClienteCaminhoArmazenamento clienteCaminhoArmazenamento) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.processoAppRepository = processoAppRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.clienteCaminhoArmazenamento = clienteCaminhoArmazenamento;
        this.prepararVinculoProcessoOficial = prepararVinculoProcessoOficial;
        this.sincronizacaoProducer = sincronizacaoProducer;
    }

    @Transactional
    public SolicitacaoDuvidaDetalhesResponse criarSolicitacao(SolicitacaoDuvidaCriacaoRequest request) {
        String numeroProcesso = request.numeroProcessoSanitizado();
        String telefone = request.telefoneSanitizado();
        String cnpj = request.cnpjSanitizado();

        Cliente cliente = new Cliente();
        String nomeRazaoSocial = (request.isPF()) ? request.nomeCompleto().trim() : request.razaoSocial().trim();
        cliente.setNomeRazaoSocial(nomeRazaoSocial);
        cliente.setPF(request.isPF());
        cliente.setComoConheceu(request.comoConheceu());
        cliente.setStatusCliente(buscarStatus(StatusClienteEnum.LEAD));
        cliente.setDataHoraEnvioFormulario(LocalDateTime.now());
        cliente = clienteRepository.save(cliente);
        clienteCaminhoArmazenamento.registrar(cliente);

        Contato contato = ContatoMapper.toEntity(
                request.nomeCompleto().trim(),
                request.email().trim(),
                telefone,
                normalizarOpcional(request.profissao()),
                true,
                cliente
        );
        contato = contatoRepository.save(contato);
        cliente.setContatos(List.of(contato));

        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setCliente(cliente);
        processoApp.setDescricaoCaso(request.descricao().trim());
        processoApp.setObservacao(OBSERVACAO_SOLICITACAO_PREFIXO);
        processoApp.setDataHoraInicio(cliente.getDataHoraEnvioFormulario());
        processoApp.setUltimaAtualizacao(cliente.getDataHoraEnvioFormulario());
        processoApp.setStatus(StatusProcesso.SOLICITACAO);

        if (!request.isPF() && cnpj != null) {
            processoApp.setCnpjInformadoFormulario(cnpj);
        }

        boolean sincronizar = numeroProcesso != null
                && prepararVinculoProcessoOficial.preparar(processoApp, numeroProcesso);

        ProcessoApp salvo = processoAppRepository.save(processoApp);
        if (sincronizar) {
            sincronizacaoProducer.enviarAposCommit(salvo.getId());
        }

        return SolicitacaoDuvidaMapper.toDetalhesResponse(
                ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(salvo)
        );
    }

    private StatusCliente buscarStatus(StatusClienteEnum statusEnum) {
        return statusClienteRepository.findByNomeIgnoreCase(statusEnum.getNome())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status de cliente não encontrado: " + statusEnum.getNome()));
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
