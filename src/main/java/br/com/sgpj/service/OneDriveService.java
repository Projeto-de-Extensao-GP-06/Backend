package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.*;
import br.com.sgpj.mapper.OneDriveMapper;
import br.com.sgpj.repository.*;
import br.com.sgpj.security.FilenameSanitizer;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.drives.item.items.item.preview.PreviewPostRequestBody;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OneDriveService {

    private static final long LIMITE_UPLOAD_SIMPLES = 4L * 1024 * 1024;

    private static final long TAMANHO_FATIA = 5L * 1024 * 1024;

    private static final String CAMINHO_PADRAO = "root:/sgpj/usuarios";

    private final OneDriveTokenAdapter tokenAdapter;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final ClienteRepository clienteRepository;
    private final DocumentoRepository documentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final ProcessoDocumentoRepository processoDocumentoRepository;

    public OneDriveService(OneDriveTokenAdapter tokenAdapter, UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter, ClienteRepository clienteRepository, DocumentoRepository documentoRepository, UsuarioRepository usuarioRepository, TipoDocumentoRepository tipoDocumentoRepository, CaminhoPadraoDeArmazenamentoRepository caminhoPadraoDeArmazenamentoRepository, ProcessoAppRepository processoAppRepository, ProcessoDocumentoRepository processoDocumentoRepository) {
        this.tokenAdapter = tokenAdapter;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.clienteRepository = clienteRepository;
        this.documentoRepository = documentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.caminhoPadraoDeArmazenamentoRepository = caminhoPadraoDeArmazenamentoRepository;
        this.processoAppRepository = processoAppRepository;
        this.processoDocumentoRepository = processoDocumentoRepository;
    }

    private Usuario obterUsuarioLogado() {
        String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new OneDriveNotFoundException("Usuário logado não encontrado: " + email));
    }

    private CaminhoPadraoDeArmazenamento obterCaminho(Long caminhoDeArmazenamentoId) {
        return caminhoPadraoDeArmazenamentoRepository.findById(caminhoDeArmazenamentoId)
                .orElseThrow(() -> new OneDriveNotFoundException("Caminho de armazenamento não encontrado com ID: " + caminhoDeArmazenamentoId));
    }

    private Documento registrarDocumento(OneDriveItemResponse item, Long tipoDocumentoId, CaminhoPadraoDeArmazenamento caminhoDeArmazenamento, Long processoAppId) {
        Usuario usuario = obterUsuarioLogado();

        TipoDocumento tipo = tipoDocumentoRepository.findById(tipoDocumentoId)
                .orElseThrow(() -> new OneDriveNotFoundException("Tipo de documento não encontrado com ID: " + tipoDocumentoId));

        Documento documento = documentoRepository.findByOnedriveItemId(item.id())
                .orElse(new Documento());

        boolean isNovo = documento.getId() == null;

        documento.setNomeOriginal(item.nome());
        documento.setMimeType(item.mimeType());
        documento.setTamanhoBytes(item.tamanhoBytes());
        documento.setOnedriveItemId(item.id());
        documento.setOnedriveUrlThumbnail(item.thumbnailUrl());
        documento.setOnedriveUrlDownload(item.urlDownload());
        documento.setUltimaAtualizacao(LocalDateTime.now());
        documento.setAtivo(true);
        documento.setPadraoArmazenamento(caminhoDeArmazenamento);
        documento.setCriadoPorUsuario(usuario);
        documento.setTipoDocumento(tipo);

        if (isNovo) {
            documento.setDataHoraCriacao(LocalDateTime.now());
        }

        Documento documentoSalvo = documentoRepository.save(documento);

        if (processoAppId != null) {
            ProcessoApp processo = processoAppRepository.findById(processoAppId)
                    .orElseThrow(() -> new OneDriveNotFoundException("Processo não encontrado: " + processoAppId));

            ProcessoDocumentoId pdId = new ProcessoDocumentoId(documentoSalvo.getId(), processoAppId);
            ProcessoDocumento pd = new ProcessoDocumento();
            pd.setId(pdId);
            pd.setDocumento(documentoSalvo);
            pd.setProcessoApp(processo);
            pd.setAnexadoPorUsuario(usuario);
            pd.setAnexadoEm(LocalDateTime.now());
            processoDocumentoRepository.save(pd);
        }

        return documentoSalvo;
    }


    public List<OneDriveItemResponse> listarArquivos(Long caminhoDeArmazenamentoId) {
        CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
        String driveId = obterDriveId();
        String caminho = CAMINHO_PADRAO + "/" + caminhoArmazenamento.getCaminho() + ":";

        List<DriveItem> itens = getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(caminho)
                .children()
                .get(config -> config.queryParameters.expand = new String[]{"thumbnails"})
                .getValue();

        return OneDriveMapper.toDtoList(itens);
    }

    public OneDriveItemResponse criarPasta(Long clienteId, String numeroCnj) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        // Sanitize CNJ number to prevent path injection attacks
        String numeroCnjSanitizado = FilenameSanitizer.sanitizeSafe(numeroCnj);

        String nomeBase = gerarNomePasta(cliente);
        String caminhoCompleto = nomeBase + "/" + numeroCnjSanitizado;

        // Verifica se já existe esse caminho no banco → 409 Conflict, não 500
        if (caminhoPadraoDeArmazenamentoRepository.existsByCaminho(caminhoCompleto)) {
            throw new CaminhoJaExisteException(caminhoCompleto);
        }

        try {
            String driveId = obterDriveId();

            // 1. Garante que a pasta do cliente existe (cria ou reutiliza sem deletar filhos)
            garantirPastaNoOneDrive(driveId, CAMINHO_PADRAO + ":", nomeBase);

            // 2. Cria a pasta do processo dentro da pasta do cliente
            String caminhoPaiProcesso = CAMINHO_PADRAO + "/" + nomeBase + ":";
            DriveItem pastaCriada = criarPastaNoOneDrive(driveId, caminhoPaiProcesso, numeroCnjSanitizado);

            // Só salva no banco DEPOIS de criar com sucesso no OneDrive
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

    /** Verifica se a pasta já existe no OneDrive; se não, cria. Não usa "replace" para evitar deletar conteúdo existente. */
    private void garantirPastaNoOneDrive(String driveId, String caminhoPai, String nomePasta) {
        try {
            // Tenta buscar a pasta; se existir, retorna sem fazer nada
            getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminhoPai + "/" + nomePasta + ":")
                    .get();
        } catch (Exception e) {
            // Pasta não existe → cria
            criarPastaNoOneDrive(driveId, caminhoPai, nomePasta);
        }
    }

    private DriveItem criarPastaNoOneDrive(String driveId, String caminhoPai, String nomePasta) {
        DriveItem novaPasta = new DriveItem();
        novaPasta.setName(nomePasta);
        novaPasta.setFolder(new Folder());
        novaPasta.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "replace"));

        return getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(caminhoPai)
                .children()
                .post(novaPasta);
    }

    private String gerarNomePasta(Cliente cliente) {
        String nome = cliente.getNomeRazaoSocial()
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");

        String documento = cliente.getCpfCnpj() != null ? cliente.getCpfCnpj() : "sem-documento";
        
        // Sanitize the generated folder name to prevent path injection
        String nomePasta = nome + "-" + documento;
        return FilenameSanitizer.sanitizeSafe(nomePasta);
    }

    // Escolhe automaticamente entre upload simples (≤ 4 MB) e upload em chunks (> 4 MB).
    public OneDriveItemResponse uploadDocumento(String nome, byte[] conteudo, Long tipoDocumentoId, Long caminhoDeArmazenamentoId, Long processoAppId) {
        // Sanitize filename to prevent path injection attacks (defense in depth)
        String nomeSanitizado = FilenameSanitizer.sanitizeSafe(nome);
        
        CaminhoPadraoDeArmazenamento caminho = obterCaminho(caminhoDeArmazenamentoId);

        if (conteudo.length <= LIMITE_UPLOAD_SIMPLES) {
            return uploadSimples(nomeSanitizado, conteudo, tipoDocumentoId, caminho, processoAppId);
        }
        return uploadGrande(nomeSanitizado, conteudo, tipoDocumentoId, caminho, processoAppId);
    }

    public void excluirArquivo(Long caminhoDeArmazenamentoId, String nomeArquivo) {
        try {
            // Sanitize filename to prevent path injection attacks
            String nomeArquivoSanitizado = FilenameSanitizer.sanitizeSafe(nomeArquivo);
            
            CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + caminhoArmazenamento.getCaminho() + "/" + nomeArquivoSanitizado + ":";

            getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .delete();

            documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue(
                    nomeArquivoSanitizado, caminhoArmazenamento.getId()
            ).ifPresent(doc -> {
                Usuario usuario = obterUsuarioLogado();
                doc.setAtivo(false);
                doc.setInativadoEm(LocalDateTime.now());
                doc.setInativadoPorUsuario(usuario);
                doc.setUltimaAtualizacao(LocalDateTime.now());
                documentoRepository.save(doc);
            });
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao excluir o arquivo: " + e.getMessage());
        }
    }

    public void excluirPasta(Long caminhoDeArmazenamentoId) {
        try {
            CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
            String driveId = obterDriveId();
            String caminhoCompleto = caminhoArmazenamento.getCaminho();

            // 1. Tenta excluir a pasta no OneDrive; se não existir, ignora e continua limpando o banco
            String caminho = CAMINHO_PADRAO + "/" + caminhoCompleto + ":";
            try {
                getClient()
                        .drives().byDriveId(driveId)
                        .items().byDriveItemId(caminho)
                        .delete();
            } catch (Exception e) {
                // Pasta pode não existir no OneDrive (resquício) — prossegue para limpar o banco de qualquer forma
            }

            // 2. Inativa todos os documentos dessa pasta e remove o vínculo com o caminho
            Usuario usuario = obterUsuarioLogado();
            List<Documento> documentos = documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(
                    caminhoArmazenamento.getId()
            );
            for (Documento doc : documentos) {
                doc.setAtivo(false);
                doc.setInativadoEm(LocalDateTime.now());
                doc.setInativadoPorUsuario(usuario);
                doc.setUltimaAtualizacao(LocalDateTime.now());
                doc.setPadraoArmazenamento(null);
            }
            if (!documentos.isEmpty()) {
                documentoRepository.saveAll(documentos);
            }

            // 3. Remove vínculo de documentos já inativos que referenciam esse caminho
            List<Documento> todosDocumentos = documentoRepository.findByPadraoArmazenamentoId(
                    caminhoArmazenamento.getId()
            );
            for (Documento doc : todosDocumentos) {
                doc.setPadraoArmazenamento(null);
            }
            if (!todosDocumentos.isEmpty()) {
                documentoRepository.saveAll(todosDocumentos);
            }

            // 4. Remove o registro do caminho de armazenamento do banco
            caminhoPadraoDeArmazenamentoRepository.delete(caminhoArmazenamento);

            // 5. Se a pasta pai do cliente ficou vazia no OneDrive, exclui ela também
            String pastaPai = caminhoCompleto.contains("/")
                    ? caminhoCompleto.substring(0, caminhoCompleto.indexOf('/'))
                    : null;

            if (pastaPai != null) {
                String caminhoPai = CAMINHO_PADRAO + "/" + pastaPai + ":";
                try {
                    List<DriveItem> filhos = getClient()
                            .drives().byDriveId(driveId)
                            .items().byDriveItemId(caminhoPai)
                            .children()
                            .get()
                            .getValue();

                    if (filhos == null || filhos.isEmpty()) {
                        getClient()
                                .drives().byDriveId(driveId)
                                .items().byDriveItemId(caminhoPai)
                                .delete();
                    }
                } catch (Exception ignored) {
                    // Pasta pai pode já não existir — não é erro crítico
                }
            }

        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao excluir a pasta: " + e.getMessage());
        }
    }

    public OneDriveItemResponse renomearArquivo(Long caminhoDeArmazenamentoId, String nomeAtual, String novoNome) {
        try {
            // Sanitize both filenames to prevent path injection attacks
            String nomeAtualSanitizado = FilenameSanitizer.sanitizeSafe(nomeAtual);
            String novoNomeSanitizado = FilenameSanitizer.sanitizeSafe(novoNome);
            
            CaminhoPadraoDeArmazenamento caminhoArmazenamento = obterCaminho(caminhoDeArmazenamentoId);
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + caminhoArmazenamento.getCaminho() + "/" + nomeAtualSanitizado + ":";

            DriveItem patch = new DriveItem();
            patch.setName(novoNomeSanitizado);

            DriveItem atualizado = getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .patch(patch);

            OneDriveItemResponse res = OneDriveMapper.toDto(atualizado, gerarPreviewUrl(atualizado.getId()));

            // Atualiza o documento no banco
            documentoRepository.findByNomeOriginalAndPadraoArmazenamentoIdAndAtivoTrue(
                    nomeAtualSanitizado, caminhoArmazenamento.getId()
            ).ifPresent(doc -> {
                doc.setNomeOriginal(novoNomeSanitizado);
                doc.setUltimaAtualizacao(LocalDateTime.now());
                doc.setOnedriveUrlDownload(res.urlDownload());
                doc.setOnedriveUrlThumbnail(res.thumbnailUrl());
                documentoRepository.save(doc);
            });

            return res;
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao renomear o arquivo: " + e.getMessage());
        }
    }

    public String gerarPreviewUrl(String itemId) {
        String driveId = obterDriveId();

        ItemPreviewInfo preview = getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(itemId)
                .preview()
                .post(new PreviewPostRequestBody());

        if (preview == null || preview.getGetUrl() == null) {
            throw new OneDriveErroException("Não foi possível gerar preview para o item: " + itemId);
        }

        return preview.getGetUrl();
    }

    /** Upload simples  — para arquivos de até 4 MB. */
    private OneDriveItemResponse uploadSimples(String nome, byte[] conteudo, Long tipoDocumentoId, CaminhoPadraoDeArmazenamento caminhoDeArmazenamento, Long processoAppId) {
        String driveId = obterDriveId();
        String caminho = CAMINHO_PADRAO + "/" + caminhoDeArmazenamento.getCaminho() + "/" + nome + ":";

        DriveItem item = getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(caminho)
                .content()
                .put(new ByteArrayInputStream(conteudo));

        OneDriveItemResponse res = OneDriveMapper.toDto(item, gerarPreviewUrl(item.getId()));
        registrarDocumento(res, tipoDocumentoId, caminhoDeArmazenamento, processoAppId);
        return res;
    }

    /** Upload em chunks via Upload Session — para arquivos maiores que 4 MB. */
    private OneDriveItemResponse uploadGrande(String nome, byte[] conteudo, Long tipoDocumentoId, CaminhoPadraoDeArmazenamento caminhoDeArmazenamento, Long processoAppId) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + caminhoDeArmazenamento.getCaminho() + "/" + nome + ":";

            CreateUploadSessionPostRequestBody corpo = new CreateUploadSessionPostRequestBody();
            DriveItemUploadableProperties propriedades = new DriveItemUploadableProperties();
            propriedades.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "replace"));
            corpo.setItem(propriedades);

            UploadSession sessao = getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .createUploadSession()
                    .post(corpo);

            InputStream stream = new ByteArrayInputStream(conteudo);
            LargeFileUploadTask<DriveItem> tarefa = new LargeFileUploadTask<>(
                    getClient().getRequestAdapter(),
                    sessao,
                    stream,
                    conteudo.length,
                    TAMANHO_FATIA,
                    DriveItem::createFromDiscriminatorValue
            );

            UploadResult<DriveItem> resultado = tarefa.upload();

            if (!resultado.isUploadSuccessful()) {
                throw new OneDriveErroException("Falha no upload do arquivo: " + nome);
            }

            DriveItem item = resultado.itemResponse;
            OneDriveItemResponse res = OneDriveMapper.toDto(item, gerarPreviewUrl(item.getId()));
            registrarDocumento(res, tipoDocumentoId, caminhoDeArmazenamento, processoAppId);
            return res;

        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao fazer upload do arquivo grande: " + e.getMessage());
        }
    }


    /** Monta o GraphServiceClient com o access token atual. */
    private GraphServiceClient getClient() {
        String token = tokenAdapter.getAccessToken();

        AccessTokenProvider tokenProvider = new AccessTokenProvider() {
            @Override
            public String getAuthorizationToken(URI uri, Map<String, Object> contexto) {
                return token;
            }

            @Override
            public AllowedHostsValidator getAllowedHostsValidator() {
                return new AllowedHostsValidator("graph.microsoft.com");
            }
        };

        return new GraphServiceClient(
                new BaseBearerTokenAuthenticationProvider(tokenProvider)
        );
    }

    /** Obtém o ID do drive do usuário autenticado. */
    private String obterDriveId() {
        Drive drive = getClient().me().drive().get();
        if (drive == null) {
            throw new OneDriveErroException("Não foi possível obter o drive do usuário.");
        }
        return drive.getId();
    }
}
