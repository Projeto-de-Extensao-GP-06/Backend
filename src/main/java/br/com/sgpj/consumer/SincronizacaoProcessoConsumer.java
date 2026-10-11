package br.com.sgpj.consumer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SincronizacaoProcessoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoProcessoConsumer.class);

    private final UpdateProcesso updateProcesso;
    private final ProcessoAppRepository processoAppRepository;

    public SincronizacaoProcessoConsumer(UpdateProcesso updateProcesso, ProcessoAppRepository processoAppRepository) {
        this.updateProcesso = updateProcesso;
        this.processoAppRepository = processoAppRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void processarSincronizacao(SincronizacaoProcessoMessage mensagem) {
        Long processoId = mensagem.processoId();
        log.info("[CONSUMER] Recebida solicitação de sincronização para o processo ID: {}", processoId);

        try {
            updateProcesso.atualizarDadosDoDataJud(processoId);
            log.info("[CONSUMER] Sincronização do processo ID {} concluída com sucesso via DataJud.", processoId);
        } catch (Exception ex) {
            log.error("[CONSUMER] Erro ou ausência de dados ao sincronizar processo ID {} no DataJud: {}", processoId, ex.getMessage());
            // Atualiza timestamp para sinalizar conclusão do processamento da fila ao frontend
            try {
                processoAppRepository.findById(processoId).ifPresent(p -> {
                    p.setUltimaAtualizacao(LocalDateTime.now());
                    processoAppRepository.save(p);
                });
            } catch (Exception e) {
                log.warn("[CONSUMER] Não foi possível atualizar timestamp de fallback para processo ID {}", processoId);
            }
        }
    }
}
