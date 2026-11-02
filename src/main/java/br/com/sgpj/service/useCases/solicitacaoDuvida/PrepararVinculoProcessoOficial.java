package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.exception.TribunalInvalidoException;
import br.com.sgpj.mapper.SiglaTribunalMapper;
import br.com.sgpj.repository.SiglaTribunalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Component
public class PrepararVinculoProcessoOficial {

    private static final Logger logger = LoggerFactory.getLogger(PrepararVinculoProcessoOficial.class);
    private final SiglaTribunalRepository siglaTribunalRepository;

    public PrepararVinculoProcessoOficial(SiglaTribunalRepository siglaTribunalRepository) {
        this.siglaTribunalRepository = siglaTribunalRepository;
    }

    public boolean preparar(ProcessoApp processoApp, String numeroProcesso) {
        String cnj = numeroProcesso == null ? "" : numeroProcesso.replaceAll("\\D", "");
        if (!cnj.matches("\\d{20}")) {
            logger.warn("Não foi possível vincular processo oficial: número CNJ inválido para o processo {}.",
                    processoApp.getId());
            return false;
        }

        try {
            SiglaTribunal tribunal = processoApp.getSiglaTribunal();
            if (tribunal == null) {
                String sigla = SiglaTribunalMapper.mapearSiglaTribunalPorNumero(cnj.substring(14, 16));
                tribunal = siglaTribunalRepository.findByNomeIgnoreCase(sigla).orElse(null);
                if (tribunal == null) {
                    logger.warn("Sigla de tribunal não encontrada para o número {}: {}", cnj, sigla);
                    return false;
                }
            }

            ProcessoApi processoApi = processoApp.getProcessoApi();
            if (processoApi == null) {
                processoApi = new ProcessoApi();
                processoApp.setProcessoApi(processoApi);
            }
            if (processoApi.getNumeroCnj() == null || processoApi.getNumeroCnj().isBlank()) {
                processoApi.setNumeroCnj(cnj);
            }
            if (processoApi.getTribunalTramitacao() == null || processoApi.getTribunalTramitacao().isBlank()) {
                processoApi.setTribunalTramitacao(tribunal.getNome());
            }
            processoApp.setSiglaTribunal(tribunal);
            return true;
        } catch (TribunalInvalidoException | DataAccessException exception) {
            logger.warn("Não foi possível vincular processo oficial. Número: {}. Motivo: {}",
                    cnj, exception.getMessage(), exception);
            return false;
        }
    }
}
