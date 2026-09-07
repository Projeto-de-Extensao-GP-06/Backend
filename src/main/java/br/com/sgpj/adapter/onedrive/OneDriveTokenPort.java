package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import java.util.Optional;

public interface OneDriveTokenPort {
    Optional<MicrosoftTokenConfig> findTokenConfig();
    MicrosoftTokenConfig createOrGetDefault();
    MicrosoftTokenConfig save(MicrosoftTokenConfig config);
}
