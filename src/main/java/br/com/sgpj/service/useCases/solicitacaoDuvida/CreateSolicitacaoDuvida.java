package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
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
import br.com.sgpj.service.useCases.processo.ProcessoApiGateway;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateSolicitacaoDuvida {

    private static final Logger logger = LoggerFactory.getLogger(CreateSolicitacaoDuvida.class);
    private static final String OBSERVACAO_SOLICITACAO_PREFIXO = "Solicitação enviada pelo formulário público.";

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final StatusClienteRepository statusClienteRepository;
    private final ProcessoApiGateway processoApiGateway;

    public CreateSolicitacaoDuvida(ClienteRepository clienteRepository,
                                    ContatoRepository contatoRepository,
                                    ProcessoAppRepository processoAppRepository,
                                    StatusClienteRepository statusClienteRepository,
                                    ProcessoApiGateway processoApiGateway) {
        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.processoAppRepository = processoAppRepository;
        this.statusClienteRepository = statusClienteRepository;
        this.processoApiGateway = processoApiGateway;
    }

    @Transactional
    public SolicitacaoDuvidaDetalhesResponse criarSolicitacao(SolicitacaoDuvidaCriacaoRequest request) {
        String numeroProcesso = normalizarOpcional(request.numeroProcesso());

        Cliente cliente = new Cliente();
        String nomeRazaoSocial = (request.isPF()) ? request.nomeCompleto() : request.razaoSocial().trim();
        cliente.setNomeRazaoSocial(nomeRazaoSocial);
        cliente.setPF(request.isPF());
        cliente.setComoConheceu(request.comoConheceu());
        cliente.setStatusCliente(buscarStatus(StatusClienteEnum.LEAD));
        cliente.setDataHoraEnvioFormulario(LocalDateTime.now());
        cliente = clienteRepository.save(cliente);

        Contato contato = ContatoMapper.toEntity(
                request.nomeCompleto().trim(),
                request.email().trim(),
                request.telefone().trim(),
                null,
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

        if (!request.isPF()) {
            processoApp.setCnpjInformadoFormulario(request.cnpj().trim());
        }

        if (numeroProcesso != null) {
            processoApp.setProcessoApi(buscarProcessoOficial(numeroProcesso));
        }

        return SolicitacaoDuvidaMapper.toDetalhesResponse(
                ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoAppRepository.save(processoApp))
        );
    }

    private StatusCliente buscarStatus(StatusClienteEnum statusEnum) {
        return statusClienteRepository.findByNomeIgnoreCase(statusEnum.getNome())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Status de cliente não encontrado: " + statusEnum.getNome()));
    }

    private ProcessoApi buscarProcessoOficial(String numeroProcesso) {
        try {
            return processoApiGateway.buscarDadosOficiais(numeroProcesso);
        } catch (RuntimeException exception) {
            logger.warn("Não foi possível buscar processo oficial. Número: {}. Motivo: {}",
                    numeroProcesso, exception.getMessage());
            return null;
        }
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
