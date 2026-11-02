package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.exception.OneDriveErroException;
import br.com.sgpj.mapper.OneDriveMapper;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import com.microsoft.graph.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.drives.item.items.item.preview.PreviewPostRequestBody;
import com.microsoft.graph.models.Drive;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.DriveItemCollectionResponse;
import com.microsoft.graph.models.DriveItemUploadableProperties;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.ItemPreviewInfo;
import com.microsoft.graph.models.UploadSession;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.authentication.AccessTokenProvider;
import com.microsoft.kiota.authentication.AllowedHostsValidator;
import com.microsoft.kiota.authentication.BaseBearerTokenAuthenticationProvider;
import com.microsoft.kiota.ApiException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Component
public class OneDriveGraphGateway {

    private final OneDriveTokenAdapter tokenAdapter;

    public OneDriveGraphGateway(OneDriveTokenAdapter tokenAdapter) {
        this.tokenAdapter = tokenAdapter;
    }

    public List<OneDriveItemResponse> listarArquivos(String caminhoCompleto) {
        try {
            String driveId = obterDriveId();

            List<DriveItem> itens = criarCliente()
                    .drives().byDriveId(driveId)
                    .items().byDriveItemId(caminhoCompleto)
                    .children()
                    .get(config -> config.queryParameters.expand = new String[]{"thumbnails"})
                    .getValue();

            return OneDriveMapper.toDtoList(itens);
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao listar arquivos do OneDrive: " + e.getMessage());
        }
    }

    public DriveItem garantirPasta(String caminhoPai, String nomePasta) {
        try {
            GraphServiceClient cliente = criarCliente();
            String driveId = obterDriveId();
            String caminhoBase = caminhoPai.endsWith(":")
                    ? caminhoPai.substring(0, caminhoPai.length() - 1) : caminhoPai;
            String caminho = caminhoBase + "/" + nomePasta + ":";
            try {
                return cliente.drives().byDriveId(driveId).items().byDriveItemId(caminho).get();
            } catch (ApiException e) {
                if (e.getResponseStatusCode() != 404) {
                    throw e;
                }
            }

            DriveItem novaPasta = new DriveItem();
            novaPasta.setName(nomePasta);
            novaPasta.setFolder(new Folder());
            novaPasta.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "fail"));
            try {
                return cliente.drives().byDriveId(driveId).items().byDriveItemId(caminhoPai)
                        .children().post(novaPasta);
            } catch (ApiException e) {
                if (e.getResponseStatusCode() != 409) {
                    throw e;
                }
                return cliente.drives().byDriveId(driveId).items().byDriveItemId(caminho).get();
            }
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao garantir pasta no OneDrive", e);
        }
    }

    public DriveItem criarPasta(String caminhoPai, String nomePasta) {
        try {
            DriveItem novaPasta = new DriveItem();
            novaPasta.setName(nomePasta);
            novaPasta.setFolder(new Folder());
            novaPasta.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "replace"));

            return criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoPai)
                    .children()
                    .post(novaPasta);
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao criar a pasta no OneDrive: " + e.getMessage());
        }
    }

    public DriveItem uploadSimples(String caminhoArquivo, byte[] conteudo) {
        try {
            return criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoArquivo)
                    .content()
                    .put(new ByteArrayInputStream(conteudo));
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao fazer upload do arquivo: " + e.getMessage());
        }
    }

    public DriveItem uploadGrande(String caminhoArquivo, byte[] conteudo) {
        try {
            CreateUploadSessionPostRequestBody corpo = new CreateUploadSessionPostRequestBody();
            DriveItemUploadableProperties propriedades = new DriveItemUploadableProperties();
            propriedades.setAdditionalData(Map.of("@microsoft.graph.conflictBehavior", "replace"));
            corpo.setItem(propriedades);

            UploadSession sessao = criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoArquivo)
                    .createUploadSession()
                    .post(corpo);

            InputStream stream = new ByteArrayInputStream(conteudo);
            LargeFileUploadTask<DriveItem> tarefa = new LargeFileUploadTask<>(
                    criarCliente().getRequestAdapter(),
                    sessao,
                    stream,
                    conteudo.length,
                    OneDrivePathHelper.TAMANHO_FATIA,
                    DriveItem::createFromDiscriminatorValue
            );

            UploadResult<DriveItem> resultado = tarefa.upload();
            if (!resultado.isUploadSuccessful()) {
                throw new OneDriveErroException("Falha no upload do arquivo.");
            }

            return resultado.itemResponse;
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao fazer upload do arquivo grande: " + e.getMessage());
        }
    }

    public void excluir(String caminhoItem) {
        try {
            criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoItem)
                    .delete();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao excluir item do OneDrive: " + e.getMessage());
        }
    }

    public DriveItem renomearArquivo(String caminhoArquivo, String novoNome) {
        try {
            DriveItem patch = new DriveItem();
            patch.setName(novoNome);

            return criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoArquivo)
                    .patch(patch);
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao renomear o arquivo: " + e.getMessage());
        }
    }

    public List<DriveItem> listarFilhos(String caminhoPasta) {
        try {
            DriveItemCollectionResponse resposta = criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(caminhoPasta)
                    .children()
                    .get();
            return resposta != null ? resposta.getValue() : List.of();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao listar filhos da pasta do OneDrive: " + e.getMessage());
        }
    }

    public String gerarPreviewUrl(String itemId) {
        try {
            ItemPreviewInfo preview = criarCliente()
                    .drives().byDriveId(obterDriveId())
                    .items().byDriveItemId(itemId)
                    .preview()
                    .post(new PreviewPostRequestBody());

            if (preview == null || preview.getGetUrl() == null) {
                throw new OneDriveErroException("Não foi possível gerar preview para o item: " + itemId);
            }

            return preview.getGetUrl();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Erro ao gerar preview do item: " + e.getMessage());
        }
    }

    public String obterDriveId() {
        try {
            Drive drive = criarCliente().me().drive().get();
            if (drive == null || drive.getId() == null) {
                throw new OneDriveErroException("Não foi possível obter o drive do usuário.");
            }
            return drive.getId();
        } catch (OneDriveErroException e) {
            throw e;
        } catch (Exception e) {
            throw new OneDriveErroException("Não foi possível obter o drive do usuário: " + e.getMessage());
        }
    }

    GraphServiceClient criarCliente() {
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

        return new GraphServiceClient(new BaseBearerTokenAuthenticationProvider(tokenProvider));
    }
}
