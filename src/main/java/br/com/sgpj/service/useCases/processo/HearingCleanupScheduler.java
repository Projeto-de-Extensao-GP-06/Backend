package br.com.sgpj.service.useCases.processo;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class HearingCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(HearingCleanupScheduler.class);
    private static final String TIMEZONE_BRASIL = "America/Sao_Paulo";
    private static final ZoneId ZONE_BRASIL = ZoneId.of(TIMEZONE_BRASIL);
    private static final DateTimeFormatter DATA_AUDIENCIA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final ProcessoAppRepository processoAppRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroHistoricoProcesso registroHistorico;

    public HearingCleanupScheduler(
            ProcessoAppRepository processoAppRepository,
            UsuarioRepository usuarioRepository,
            RegistroHistoricoProcesso registroHistorico
    ) {
        this.processoAppRepository = processoAppRepository;
        this.usuarioRepository = usuarioRepository;
        this.registroHistorico = registroHistorico;
    }

    @Scheduled(cron = "0 0 2 * * *", zone = TIMEZONE_BRASIL)
    @SchedulerLock(
            name = "HearingCleanupScheduler_processarAudienciasPassadas",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT15M"
    )
    @Transactional
    public void processarAudienciasPassadas() {
        processarAudienciasPassadas(LocalDateTime.now(ZONE_BRASIL));
    }

    @Transactional
    public void processarAudienciasPassadas(LocalDateTime dataReferencia) {
        List<ProcessoApp> processos = processoAppRepository.buscarAudienciasPassadas(
                StatusProcesso.ATIVO, dataReferencia);

        if (processos.isEmpty()) {
            return;
        }

        Usuario usuarioSistema = obterUsuarioSistema();

        for (ProcessoApp processo : processos) {
            LocalDateTime dataAudienciaRealizada = processo.getDataAudiencia();
            String descricao = "Audiência realizada em "
                    + dataAudienciaRealizada.format(DATA_AUDIENCIA_FORMATTER) + ".";

            processo.setDataAudiencia(null);
            processo.setUltimaAtualizacao(dataReferencia);
            processoAppRepository.save(processo);

            registroHistorico.registrar(
                    processo,
                    usuarioSistema,
                    RegistroHistoricoProcesso.AUDIENCIA,
                    descricao
            );

            log.info("Audiência passada limpa e registrada no histórico para o processo ID {}.",
                    processo.getId());
        }
    }

    private Usuario obterUsuarioSistema() {
        return usuarioRepository.findById(Usuario.ID_SISTEMA)
                .filter(Usuario::isUsuarioSistema)
                .or(() -> Optional.ofNullable(usuarioRepository.findByEmail(Usuario.EMAIL_SISTEMA)))
                .orElseThrow(() -> new ObjetoNaoEncontradoException(
                        "Usuário SISTEMA não encontrado para registrar histórico de audiência."));
    }
}
