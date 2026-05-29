package br.com.sgpj.mapper;

import br.com.sgpj.dto.OneDriveItemResponse;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.ThumbnailSet;

import java.util.Collections;
import java.util.List;

public class OneDriveMapper {

    private OneDriveMapper() {}

    public static OneDriveItemResponse toDto(DriveItem item, String thumbnailUrl) {
        return toDto(item, thumbnailUrl, null);
    }

    public static OneDriveItemResponse toDto(DriveItem item, String thumbnailUrl, String caminhoOverride) {
        if (item == null) return null;

        String tipo = item.getFolder() != null ? "pasta" : "arquivo";

        String mimeType = null;
        if (item.getFile() != null && item.getFile().getMimeType() != null) {
            mimeType = item.getFile().getMimeType();
        }

        String caminho;
        if (caminhoOverride != null) {
            caminho = caminhoOverride;
        } else {
            caminho = null;
            if (item.getParentReference() != null && item.getParentReference().getPath() != null) {
                String rawPath = item.getParentReference().getPath() + "/" + item.getName();
                String marker = "root:/sgpj/usuarios/";
                int idx = rawPath.indexOf(marker);
                caminho = idx >= 0 ? rawPath.substring(idx + marker.length()) : rawPath;
            }
        }

        String urlDownload = null;
        if (item.getAdditionalData() != null) {
            Object url = item.getAdditionalData().get("@microsoft.graph.downloadUrl");
            if (url != null) {
                urlDownload = url.toString();
            }
        }


        return new OneDriveItemResponse(
                item.getId(),
                item.getName(),
                tipo,
                item.getSize(),
                mimeType,
                urlDownload,
                thumbnailUrl,
                item.getCreatedDateTime(),
                item.getLastModifiedDateTime(),
                caminho
        );
    }

    public static List<OneDriveItemResponse> toDtoList(List<DriveItem> items) {
        if (items == null) return Collections.emptyList();
        return items.stream()
                .map(item -> toDto(item, extrairThumbnail(item)))
                .toList();
    }

    private static String extrairThumbnail(DriveItem item) {
        if (item.getThumbnails() == null || item.getThumbnails().isEmpty()) {
            return null;
        }
        ThumbnailSet set = item.getThumbnails().getFirst();
        if (set.getLarge() != null && set.getLarge().getUrl() != null) {
            return set.getLarge().getUrl();
        }
        if (set.getMedium() != null && set.getMedium().getUrl() != null) {
            return set.getMedium().getUrl();
        }
        if (set.getSmall() != null && set.getSmall().getUrl() != null) {
            return set.getSmall().getUrl();
        }
        return null;
    }
}
