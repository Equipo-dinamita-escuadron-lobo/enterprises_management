package com.enterprises_management.copy.infraestructure.adapters.input.rest;

import com.enterprises_management.copy.application.input.ICopyProcessQueryPort;
import com.enterprises_management.copy.infraestructure.adapters.output.notifier.SseEmitterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controlador REST para el stream SSE de eventos de un proceso de copia (REQ-API-04).
 * <p>
 * Prefijo: {@code /api/enterprises/copy}
 * <p>
 * El stream se cierra automáticamente cuando el proceso alcanza un estado terminal
 * (COMPLETADO, ERROR, CANCELADO) — manejado por NotificadorEstadoSseAdapter.
 *
 * <p>ADR-8: shape del payload SSE:
 * {@code {"tipo":"...", "idProceso":"...", "ts":"...", "payload":"...", "secuencia":N}}
 *
 * <p>D-R6: límite de 50 conexiones concurrentes evaluado en Hito 7 (posible WebFlux).
 */
@RestController
@RequestMapping("/api/enterprises/copy")
@ConditionalOnProperty(name = "app.copy.orchestrator.enabled", havingValue = "true")
public class CopyProcessStreamController {

    private static final Logger log = LoggerFactory.getLogger(CopyProcessStreamController.class);

    /** Timeout SSE en milisegundos: 5 minutos. Los procesos de copia pueden durar varios minutos. */
    private static final long SSE_TIMEOUT_MS = 5L * 60 * 1000;

    private final ICopyProcessQueryPort queryPort;
    private final SseEmitterRegistry sseEmitterRegistry;

    public CopyProcessStreamController(
            ICopyProcessQueryPort queryPort,
            SseEmitterRegistry sseEmitterRegistry
    ) {
        this.queryPort = queryPort;
        this.sseEmitterRegistry = sseEmitterRegistry;
    }

    // -------------------------------------------------------------------------
    // GET /processes/{id}/stream — Stream SSE (REQ-API-04)
    // -------------------------------------------------------------------------

    /**
     * Abre un stream SSE para el proceso indicado.
     * Verifica que el proceso exista antes de abrir el stream (lanza 404 si no existe).
     *
     * @param id ID del proceso al que suscribirse
     * @return SseEmitter conectado al SseEmitterRegistry del proceso
     */
    @GetMapping(value = "/processes/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('Backup_View')")
    public SseEmitter streamEventos(@PathVariable String id) {
        // Verificar existencia del proceso — lanza CopyProcessNotFoundException (404) si no existe
        queryPort.consultarProceso(id);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        sseEmitterRegistry.register(id, emitter);

        log.info("Cliente SSE suscrito al proceso {}", id);

        emitter.onCompletion(() ->
                log.debug("SSE completado para proceso {}", id));
        emitter.onTimeout(() ->
                log.debug("SSE timeout para proceso {}", id));
        emitter.onError(ex ->
                log.warn("SSE error para proceso {}: {}", id, ex.getMessage()));

        return emitter;
    }
}
