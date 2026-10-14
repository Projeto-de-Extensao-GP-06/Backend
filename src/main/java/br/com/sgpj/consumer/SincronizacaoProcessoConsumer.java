package br.com.sgpj.consumer;

import br.com.sgpj.config.RabbitMQConfig;
import br.com.sgpj.dto.request.SincronizacaoProcessoMessage;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.SincronizacaoStatusService;
import br.com.sgpj.service.useCases.processo.UpdateProcesso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class SincronizacaoProcessoConsumer {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoProcessoConsumer.class);

    private final UpdateProcesso updateProcesso;
    private final ProcessoAppRepository processoAppRepository;
    private final SincronizacaoStatusService statusService;

    public SincronizacaoProcessoConsumer(UpdateProcesso updateProcesso,
                                         ProcessoAppRepository processoAppRepository,
                                         SincronizacaoStatusService statusService) {
        this.updateProcesso = updateProcesso;
        this.processoAppRepository = processoAppRepository;
        this.statusService = statusService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void processarSincronizacao(SincronizacaoProcessoMessage mensagem) {
        Long processoId = mensagem.processoId();

        System.out.println("============================================================");
        System.out.println("[RABBITMQ CONSUMER] 📤 Pedido SAIU da fila! Iniciando processamento...");
        System.out.println("Processo ID: " + processoId + " | Consultando API DataJud...");
        System.out.println("============================================================");

        log.info("[CONSUMER] Recebida solicitação de sincronização para o processo ID: {}", processoId);

        // Notifica o frontend em tempo real que o consumer iniciou a busca externa
        statusService.notificar(processoId, "PROCESSANDO", Map.of(
                "processoId", processoId,
                "status", "PROCESSANDO",
                "mensagem", "Buscando dados oficiais do processo no DataJud..."
        ));

        try {
            updateProcesso.atualizarDadosDoDataJud(processoId);

            System.out.println("============================================================");
            System.out.println("[RABBITMQ CONSUMER] ✅ Sincronização CONCLUÍDA com sucesso!");
            System.out.println("Processo ID: " + processoId + " | Dados oficiais atualizados e salvos.");
            System.out.println("============================================================");

            log.info("[CONSUMER] Sincronização do processo ID {} concluída com sucesso via DataJud.", processoId);

            // Notifica o frontend em tempo real com evento de conclusão
            statusService.notificar(processoId, "CONCLUIDO", Map.of(
                    "processoId", processoId,
                    "status", "SUCESSO",
                    "mensagem", "Dados oficiais do processo sincronizados com sucesso."
            ));

        } catch (Exception ex) {
            System.out.println("============================================================");
            System.out.println("[RABBITMQ CONSUMER] ⚠️ Finalizado com aviso/erro para processo ID: " + processoId);
            System.out.println("Detalhe: " + ex.getMessage());
            System.out.println("============================================================");

            log.error("[CONSUMER] Erro ou ausência de dados ao sincronizar processo ID {} no DataJud: {}", processoId, ex.getMessage());

            try {
                processoAppRepository.findById(processoId).ifPresent(p -> {
                    p.setUltimaAtualizacao(LocalDateTime.now());
                    processoAppRepository.save(p);
                });
            } catch (Exception ignored) {}

            // Notifica o frontend em tempo real que a tentativa foi concluída
            statusService.notificar(processoId, "CONCLUIDO", Map.of(
                    "processoId", processoId,
                    "status", "AVISO",
                    "mensagem", "Sincronização concluída (DataJud finalizado)."
            ));
        }
    }
}
