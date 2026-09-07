package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.exception.CaminhoJaExisteException;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.mapper.OneDriveMapper;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.useCases.onedrive.OneDriveDocumentManager;
import br.com.sgpj.service.useCases.onedrive.OneDriveGraphGateway;
import br.com.sgpj.service.useCases.onedrive.OneDrivePathHelper;
import com.microsoft.graph.models.DriveItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OneDriveService {

    private final ClienteRepository clienteRepository;
    private final CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository;
    private final OneDriveGraphGateway graphGateway;
    private final OneDriveDocumentManager documentManager;

    public OneDriveService(OneDriveTokenAdapter tokenAdapter,
                           UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                           ClienteRepository clienteRepository,
                           DocumentoRepository documentoRepository,
                           UsuarioRepository usuarioRepository,
                           TipoDocumentoRepository tipoDocumentoRepository,
                           CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository,
                           ProcessoAppRepository processoAppRepository,
                           ProcessoDocumentoRepository processoDocumentoRepository) {
        this.clienteRepository = clienteRepository;
        this.caminhoPadraoDeArmazenamentoRepository = caminhoPadraoDeArmazenamentoRepository;
        this.graphGateway = new OneDriveGraphGateway(tokenAdapter);
        this.documentManager = new OneDriveDocumentManager(
                usuarioAutenticacaoAdapter,
                usuarioRepository,
                documentoRepository,
                tipoDocumentoRepository,
                caminhoPadraoDeArmazenamentoRepository,
                processoAppRepository,
                processoDocumentoRepository
        );
    }

    public List<OneDriveItemResponse> listarArquivos(Long caminhoDeArmazenamentoId) {
        CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
        return graphGateway.listarArquivos(OneDrivePathHelper.caminhoDaPasta(caminhoArmazenamento.getCaminho()));
    }

    public OneDriveItemResponse criarPasta(Long clienteId, String numeroCnj) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        String numeroCnjSanitizado = OneDrivePathHelper.sanitizar(numeroCnj);
        String nomeBase = OneDrivePathHelper.gerarNomePasta(cliente);
        String caminhoCompleto = OneDrivePathHelper.caminhoCompletoDaPasta(nomeBase, numeroCnjSanitizado);

        if (caminhoPadraoDeArmazenamentoRepository.existsByCaminho(caminhoCompleto)) {
            throw new CaminhoJaExisteException(caminhoCompleto);
        }

        try {
            graphGateway.garantirPasta(OneDrivePathHelper.caminhoRaiz() + ":", nomeBase);
            DriveItem pastaCriada = graphGateway.criarPasta(OneDrivePathHelper.caminhoDaPasta(nomeBase), numeroCnjSanitizado);

            CaminhoPadraoDeArmazenamento novoCaminho = new CaminhoPadraoDeArmazenamento();
            novoCaminho.setCliente(cliente);
            novoCaminho.setCaminho(caminhoCompleto);
            caminhoPadraoDeArmazenamentoRepository.save(novoCaminho);

            return OneDriveMapper.toDto(pastaCriada, null, caminhoCompleto);
        } catch (CaminhoJaExisteException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao criar a pasta: " + e.getMessage());
        }
    }

    public OneDriveItemResponse uploadDocumento(String nome,
                                                byte[] conteudo,
                                                Long tipoDocumentoId,
                                                Long caminhoDeArmazenamentoId,
                                                Long processoAppId) {
        String nomeSanitizado = OneDrivePathHelper.sanitizar(nome);
        CaminhoPadraoDeArmazenamento caminho = obterCaminho(caminhoDeArmazenamentoId);
        String caminhoArquivo = OneDrivePathHelper.caminhoDoArquivo(caminho.getCaminho(), nomeSanitizado);

        DriveItem item = conteudo.length <= OneDrivePathHelper.LIMITE_UPLOAD_SIMPLES
                ? graphGateway.uploadSimples(caminhoArquivo, conteudo)
                : graphGateway.uploadGrande(caminhoArquivo, conteudo);

        OneDriveItemResponse resposta = OneDriveMapper.toDto(item, graphGateway.gerarPreviewUrl(item.getId()));
        documentManager.registrarDocumento(resposta, tipoDocumentoId, caminho, processoAppId);
        return resposta;
    }

    public void excluirArquivo(Long caminhoDeArmazenamentoId, String nomeArquivo) {
        String nomeArquivoSanitizado = OneDrivePathHelper.sanitizar(nomeArquivo);
        CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
        String caminhoArquivo = OneDrivePathHelper.caminhoDoArquivo(caminhoArmazenamento.getCaminho(), nomeArquivoSanitizado);

        graphGateway.excluir(caminhoArquivo);
        documentManager.inativarDocumentoPorArquivo(caminhoArmazenamento, nomeArquivoSanitizado);
    }

    public void excluirPasta(Long caminhoDeArmazenamentoId) {
        CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
        String caminhoCompleto = caminhoArmazenamento.getCaminho();
        String caminhoPasta = OneDrivePathHelper.caminhoDaPasta(caminhoCompleto);

        try {
            graphGateway.excluir(caminhoPasta);
        } catch (OneDriveErroException ignored) {
            // Se a pasta já não existir no OneDrive, seguimos limpando o banco.
        }

        documentManager.inativarDocumentosDaPasta(caminhoArmazenamento);
        documentManager.desvincularDocumentosDaPasta(caminhoArmazenamento);
        documentManager.removerCaminho(caminhoArmazenamento);

        String pastaPai = OneDrivePathHelper.extrairPastaPai(caminhoCompleto);
        if (pastaPai != null) {
            String caminhoPai = OneDrivePathHelper.caminhoDaPasta(pastaPai);
            try {
                if (graphGateway.listarFilhos(caminhoPai).isEmpty()) {
                    graphGateway.excluir(caminhoPai);
                }
            } catch (OneDriveErroException ignored) {
                // Pasta pai pode ter sido removida em outro fluxo.
            }
        }
    }

    public OneDriveItemResponse renomearArquivo(Long caminhoDeArmazenamentoId, String nomeAtual, String novoNome) {
        String nomeAtualSanitizado = OneDrivePathHelper.sanitizar(nomeAtual);
        String novoNomeSanitizado = OneDrivePathHelper.sanitizar(novoNome);
        CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
        String caminhoArquivo = OneDrivePathHelper.caminhoDoArquivo(caminhoArmazenamento.getCaminho(), nomeAtualSanitizado);

        DriveItem atualizado = graphGateway.renomearArquivo(caminhoArquivo, novoNomeSanitizado);
        OneDriveItemResponse resposta = OneDriveMapper.toDto(atualizado, graphGateway.gerarPreviewUrl(atualizado.getId()));

        documentManager.atualizarDocumentoRenomeado(caminhoArmazenamento, nomeAtualSanitizado, resposta);
        return resposta;
    }

    public String gerarPreviewUrl(String itemId) {
        return graphGateway.gerarPreviewUrl(itemId);
    }

    private CaminhoPadraoDeArmazenamento obterCaminho(Long caminhoDeArmazenamentoId) {
        return caminhoPadraoDeArmazenamentoRepository.findById(caminhoDeArmazenamentoId)
                .orElseThrow(() -> new OneDriveErroException(
                        "Caminho de armazenamento não encontrado com ID: " + caminhoDeArmazenamentoId));
    }
}
