package br.com.sgpj.adapter.onedrive;

import br.com.sgpj.entity.onedrive.MicrosoftTokenConfig;
import br.com.sgpj.repository.MicrosoftTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OneDriveTokenRepositoryAdapter implements OneDriveTokenPort {

    private static final long TOKEN_CONFIG_ID = 1L;
    private final MicrosoftTokenRepository repository;

    public OneDriveTokenRepositoryAdapter(MicrosoftTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<MicrosoftTokenConfig> findTokenConfig() {
        return repository.findById(TOKEN_CONFIG_ID);
    }

    @Override
    public MicrosoftTokenConfig createOrGetDefault() {
        return repository.findById(TOKEN_CONFIG_ID).orElseGet(() -> {
            MicrosoftTokenConfig c = new MicrosoftTokenConfig();
            c.setId(TOKEN_CONFIG_ID);
            return c;
        });
    }

    @Override
    public MicrosoftTokenConfig save(MicrosoftTokenConfig config) {
        return repository.save(config);
    }
}
