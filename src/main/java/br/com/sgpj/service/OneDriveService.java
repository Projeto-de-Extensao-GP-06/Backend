package br.com.sgpj.service;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.OneDriveItemResponse;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.mapper.OneDriveMapper;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.drives.item.items.item.preview.PreviewPostRequestBody;
import com.microsoft.graph.models.*;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class OneDriveService {

    private static final long LIMITE_UPLOAD_SIMPLES = 4L * 1024 * 1024;

    // Tamanho de cada fatia no upload em chunks. Múltiplo de 320 KiB (padrão do SDK: 5 MiB)
    private static final long TAMANHO_FATIA = 5L * 1024 * 1024;

    // Caminho base no OneDrive
    private static final String CAMINHO_PADRAO = "root:/sgpj/usuarios";

    private final OneDriveTokenAdapter tokenAdapter;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public OneDriveService(OneDriveTokenAdapter tokenAdapter, UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter) {
        this.tokenAdapter = tokenAdapter;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    public boolean isCorrectScope(HttpServletRequest httpRequest, String scope) {
        return usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope);
    }

    // Lista os arquivos de uma pasta (com thumbnails incluídos).
    public List<OneDriveItemResponse> listarArquivos(String nomePasta) {
        String driveId = obterDriveId();
        String caminho = CAMINHO_PADRAO + "/" + nomePasta + ":";

        List<DriveItem> itens = getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(caminho)
                .children()
                .get(config -> config.queryParameters.expand = new String[]{"thumbnails"})
                .getValue();

        return OneDriveMapper.toDtoList(itens);
    }

    // Cria uma pasta dentro do caminho padrão. Se já existir, retorna a existente.
    public OneDriveItemResponse criarPasta(String nomePasta) {
        try {
            String driveId = obterDriveId();
            String caminhoPai = CAMINHO_PADRAO + ":";

            DriveItem novaPasta = new DriveItem();
            novaPasta.setName(nomePasta);
            novaPasta.setFolder(new Folder());
            novaPasta.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "replace"));

            DriveItem criada = getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminhoPai)
                    .children()
                    .post(novaPasta);

            return OneDriveMapper.toDto(criada, null);
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao criar a pasta: " + e.getMessage());
        }
    }

    // Escolhe automaticamente entre upload simples (≤ 4 MB) e upload em chunks (> 4 MB).
    public OneDriveItemResponse uploadDocumento(String pasta, String nome, byte[] conteudo) {
        if (conteudo.length <= LIMITE_UPLOAD_SIMPLES) {
            return uploadSimples(pasta, nome, conteudo);
        }
        return uploadGrande(pasta, nome, conteudo);
    }

    // Exclui um arquivo do OneDrive.
    public void excluirArquivo(String nomePasta, String nomeArquivo) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + nomePasta + "/" + nomeArquivo + ":";

            getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .delete();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao excluir o arquivo: " + e.getMessage());
        }
    }

    // Exclui uma pasta (e todo seu conteúdo) do OneDrive.
    public void excluirPasta(String nomePasta) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + nomePasta + ":";

            getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .delete();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao excluir a pasta: " + e.getMessage());
        }
    }

    // Renomeia uma pasta no OneDrive.
    public OneDriveItemResponse renomearPasta(String nomeAtual, String novoNome) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + nomeAtual + ":";

            DriveItem patch = new DriveItem();
            patch.setName(novoNome);

            DriveItem atualizado = getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .patch(patch);

            return OneDriveMapper.toDto(atualizado, null);
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao renomear a pasta: " + e.getMessage());
        }
    }

    // Renomeia um arquivo no OneDrive.
    public OneDriveItemResponse renomearArquivo(String nomePasta, String nomeAtual, String novoNome) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + nomePasta + "/" + nomeAtual + ":";

            DriveItem patch = new DriveItem();
            patch.setName(novoNome);

            DriveItem atualizado = getClient()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminho)
                    .patch(patch);

            return OneDriveMapper.toDto(atualizado, gerarPreviewUrl(atualizado.getId()));
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao renomear o arquivo: " + e.getMessage());
        }
    }

    // Gera uma URL de preview (visualização) para um item do OneDrive.
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

    /** Upload simples via PUT /content — para arquivos de até 4 MB. */
    private OneDriveItemResponse uploadSimples(String pasta, String nome, byte[] conteudo) {
        String driveId = obterDriveId();
        String caminho = CAMINHO_PADRAO + "/" + pasta + "/" + nome + ":";

        DriveItem item = getClient()
                .drives().byDriveId(driveId)
                .items().byDriveItemId(caminho)
                .content()
                .put(new ByteArrayInputStream(conteudo));

        return OneDriveMapper.toDto(item, gerarPreviewUrl(item.getId()));
    }

    /** Upload em chunks via Upload Session — para arquivos maiores que 4 MB. */
    private OneDriveItemResponse uploadGrande(String pasta, String nome, byte[] conteudo) {
        try {
            String driveId = obterDriveId();
            String caminho = CAMINHO_PADRAO + "/" + pasta + "/" + nome + ":";

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
            return OneDriveMapper.toDto(item, gerarPreviewUrl(item.getId()));

        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao fazer upload do arquivo grande: " + e.getMessage());
        }
    }
}
