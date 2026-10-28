package br.com.sgpj.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SincronizacaoStatusService {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoStatusService.class);

    // Mapa de processoId -> lista de emitters SSE conectados
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter criarConexao(Long processoId) {
        // Timeout de 2 minutos (DataJud pode levar até 40s)
        SseEmitter emitter = new SseEmitter(120_000L);

        emitters.computeIfAbsent(processoId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removerEmitter(processoId, emitter));
        emitter.onTimeout(() -> removerEmitter(processoId, emitter));
        emitter.onError(e -> removerEmitter(processoId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("CONECTADO")
                    .data(Map.of("processoId", processoId, "mensagem", "Canal de tempo real conectado.")));
        } catch (IOException e) {
            removerEmitter(processoId, emitter);
        }

        return emitter;
    }

    public void notificar(Long processoId, String evento, Map<String, Object> payload) {
        List<SseEmitter> lista = emitters.get(processoId);
        if (lista == null || lista.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event()
                        .name(evento)
                        .data(payload));
            } catch (Exception ex) {
                removerEmitter(processoId, emitter);
            }
        }
    }

    private void removerEmitter(Long processoId, SseEmitter emitter) {
        List<SseEmitter> lista = emitters.get(processoId);
        if (lista != null) {
            lista.remove(emitter);
            if (lista.isEmpty()) {
                emitters.remove(processoId);
            }
        }
    }
}
