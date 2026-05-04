package com.enterprises_management.copy.infraestructure.adapters.output.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registro en-memoria de SseEmitters por proceso.
 * Permite que múltiples clientes se suscriban al stream SSE de un mismo proceso.
 *
 * <p>Thread-safe: usa ConcurrentHashMap y CopyOnWriteArrayList.
 *
 * @see <a href="https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html">Spring SSE</a>
 *
 * ADR-8: forma parte del pipeline de notificación SSE.
 * D-R6: límite de 50 conexiones evaluado en Hito 7 (posible migración a WebFlux).
 */
public class SseEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);

    /** Mapa: idProceso → lista de emitters activos. */
    private final ConcurrentHashMap<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Registra un emitter para el proceso dado.
     *
     * @param idProceso ID del proceso al que pertenece el emitter
     * @param emitter   emitter SSE a registrar
     */
    public void register(String idProceso, SseEmitter emitter) {
        emitters.computeIfAbsent(idProceso, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(idProceso, emitter));
        emitter.onTimeout(() -> removeEmitter(idProceso, emitter));
        emitter.onError(e -> removeEmitter(idProceso, emitter));
        log.debug("Emitter registrado para proceso {}", idProceso);
    }

    /**
     * Envía un payload String a todos los emitters del proceso.
     * Los emitters que fallen se eliminan del registro.
     *
     * @param idProceso ID del proceso
     * @param payload   payload a enviar (shape ADR-8)
     */
    public void send(String idProceso, String payload) {
        List<SseEmitter> lista = emitters.getOrDefault(idProceso, List.of());
        List<SseEmitter> fallidos = new ArrayList<>();

        for (SseEmitter emitter : lista) {
            try {
                emitter.send(SseEmitter.event().data(payload));
            } catch (IOException e) {
                log.warn("Error enviando SSE a proceso {}: {}", idProceso, e.getMessage());
                fallidos.add(emitter);
            }
        }

        fallidos.forEach(e -> removeEmitter(idProceso, e));
    }

    /**
     * Completa y elimina todos los emitters del proceso (estado terminal alcanzado).
     *
     * @param idProceso ID del proceso que alcanzó estado terminal
     */
    public void complete(String idProceso) {
        List<SseEmitter> lista = emitters.remove(idProceso);
        if (lista != null) {
            lista.forEach(e -> {
                try {
                    e.complete();
                } catch (Exception ex) {
                    log.debug("Error al completar emitter para proceso {}: {}", idProceso, ex.getMessage());
                }
            });
        }
        log.debug("Emitters completados para proceso {}", idProceso);
    }

    private void removeEmitter(String idProceso, SseEmitter emitter) {
        List<SseEmitter> lista = emitters.get(idProceso);
        if (lista != null) {
            lista.remove(emitter);
            if (lista.isEmpty()) {
                emitters.remove(idProceso);
            }
        }
    }
}
