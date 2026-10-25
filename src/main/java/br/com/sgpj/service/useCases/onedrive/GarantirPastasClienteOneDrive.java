package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import com.microsoft.kiota.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GarantirPastasClienteOneDrive {

    private static final Logger logger = LoggerFactory.getLogger(GarantirPastasClienteOneDrive.class);

    private final CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    private final ClienteCaminhoArmazenamento registrador;
    private final OneDriveGraphGateway graphGateway;

    public GarantirPastasClienteOneDrive(CaminhoPadraoDeArmazenamentoRepository caminhoRepository,
                                        ClienteCaminhoArmazenamento registrador,
                                        OneDriveGraphGateway graphGateway) {
        this.caminhoRepository = caminhoRepository;
        this.registrador = registrador;
        this.graphGateway = graphGateway;
    }

    public void garantir(Cliente cliente, Long solicitacaoId) {
        List<String> caminhos = buscarCaminhosBase(cliente.getId());
        if (caminhos.isEmpty()) {
            registrador.registrar(cliente);
            caminhos = buscarCaminhosBase(cliente.getId());
        }
        if (caminhos.isEmpty()) {
            logger.warn("Sem caminho base vinculado ao cliente {} no aceite da solicitacao {}. "
                    + "Verifique possivel colisao de caminho com outro cliente.", cliente.getId(), solicitacaoId);
        }
        for (String caminho : caminhos) {
            try {
                graphGateway.garantirPasta(OneDrivePathHelper.caminhoRaiz() + ":", caminho);
            } catch (OneDriveErroException e) {
                logger.warn("Falha ao garantir pasta no OneDrive. Cliente: {}, solicitacao: {}, caminho: {}, causa: {}, HTTP: {}",
                        cliente.getId(), solicitacaoId, caminho,
                        e.getCause() == null ? e.getClass().getSimpleName() : e.getCause().getClass().getSimpleName(),
                        e.getCause() instanceof ApiException apiException ? apiException.getResponseStatusCode() : null);
            }
        }
    }

    private List<String> buscarCaminhosBase(Long clienteId) {
        return caminhoRepository.findByClienteId(clienteId).stream()
                .map(caminho -> caminho.getCaminho())
                .filter(caminho -> !caminho.contains("/") && !caminho.contains("\\"))
                .distinct()
                .toList();
    }
}
