package com.enterprises_management.copy.infraestructure.adapters.output.notifier;

import com.enterprises_management.copy.application.output.IProcessNotifierPort;
import com.enterprises_management.copy.domain.enums.CopyEventType;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adaptador de salida que notifica eventos del proceso de copia a través de SSE.
 * Implementa IProcessNotifierPort delegando al SseEmitterRegistry.
 *
 * <p>Shape del payload enviado (ADR-8):
 * <pre>
 * {
 *   "tipo": "Fase.completada",
 *   "idProceso": "uuid",
 *   "ts": "2026-04-28T10:00:00",
 *   "payload": "...",
 *   "secuencia": 42
 * }
 * </pre>
 *
 * <p>ADR-24 (Hito 2): usa {@link ObjectMapper#writeValueAsString} para serializar el JSON.
 * Reemplaza la concatenación manual de strings del Hito 1, que era insuficiente para
 * caracteres especiales como {@code \n}, {@code \r}, {@code \}, comillas dobles y unicode.
 */
public class NotificadorEstadoSseAdapter implements IProcessNotifierPort {

    private static final Logger log = LoggerFactory.getLogger(NotificadorEstadoSseAdapter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Tipos de evento que corresponden a estados terminales del proceso (cierran el stream SSE). */
    private static final Set<CopyEventType> EVENTOS_TERMINALES = Set.of(
        CopyEventType.PROCESO_COMPLETADO,
        CopyEventType.PROCESO_ERROR,
        CopyEventType.PROCESO_CANCELADO
    );

    private final SseEmitterRegistry registry;
    private final ObjectMapper objectMapper;

    /**
     * Constructor principal (ADR-24): usa ObjectMapper para serialización segura (REQ-SSE-01).
     *
     * @param registry     registro de emitters SSE
     * @param objectMapper mapper Jackson para serialización JSON segura
     */
    public NotificadorEstadoSseAdapter(SseEmitterRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    /**
     * Constructor de conveniencia — usa ObjectMapper por defecto.
     * Mantiene compatibilidad con código que no inyecta ObjectMapper.
     *
     * @param registry registro de emitters SSE
     */
    public NotificadorEstadoSseAdapter(SseEmitterRegistry registry) {
        this(registry, new ObjectMapper());
    }

    @Override
    public void publicar(CopyProcessEvent evento) {
        String payload = construirPayload(evento);
        log.debug("Publicando evento SSE tipo={} para proceso={}", evento.getTipoEvento(), evento.getIdProceso());

        registry.send(evento.getIdProceso(), payload);

        // Si el evento es terminal, cerrar todos los emitters del proceso
        if (EVENTOS_TERMINALES.contains(evento.getTipoEvento())) {
            log.info("Evento terminal {} para proceso {} — cerrando emitters SSE",
                    evento.getTipoEvento(), evento.getIdProceso());
            registry.complete(evento.getIdProceso());
        }
    }

    /**
     * Construye el payload JSON del evento usando ObjectMapper (ADR-24, REQ-SSE-01).
     *
     * <p>El campo {@code payload} contiene el string original del evento tal como fue
     * generado por el SagaEngineService. Al serializarlo como valor de campo JSON,
     * ObjectMapper escapa automáticamente todos los caracteres especiales:
     * comillas dobles, saltos de línea, retorno de carro, backslash y unicode.
     *
     * @param evento evento del proceso
     * @return JSON string con shape ADR-8, con serialización segura
     */
    private String construirPayload(CopyProcessEvent evento) {
        String ts = evento.getOcurridoEn() != null
                ? evento.getOcurridoEn().format(FORMATTER)
                : "";
        String payloadJson = evento.getPayloadJson() != null ? evento.getPayloadJson() : "";
        Long secuencia = evento.getId() != null ? evento.getId() : 0L;

        // Construir el mapa y serializar con Jackson para escape correcto (ADR-24)
        Map<String, Object> campos = new HashMap<>();
        campos.put("tipo", evento.getTipoEvento().getTipo());
        campos.put("idProceso", evento.getIdProceso());
        campos.put("ts", ts);
        campos.put("payload", payloadJson);
        campos.put("secuencia", secuencia);

        try {
            return objectMapper.writeValueAsString(campos);
        } catch (JsonProcessingException ex) {
            // Fallback defensivo: si Jackson falla (no debería), loguear y retornar JSON mínimo
            log.error("Error al serializar evento SSE tipo={} idProceso={}: {}",
                    evento.getTipoEvento(), evento.getIdProceso(), ex.getMessage());
            return "{\"tipo\":\"error\",\"idProceso\":\"" + evento.getIdProceso() + "\",\"secuencia\":" + secuencia + "}";
        }
    }
}
