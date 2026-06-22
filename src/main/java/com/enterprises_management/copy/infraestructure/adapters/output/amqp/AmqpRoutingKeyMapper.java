package com.enterprises_management.copy.infraestructure.adapters.output.amqp;

import com.enterprises_management.copy.domain.enums.CopyEventType;

/**
 * Mapeo de {@link CopyEventType} a routing keys AMQP (REQ-EVENT-01, ADR-23).
 *
 * <p>Tabla de routing keys del exchange {@code copy.process.exchange}:
 * <ul>
 *   <li>{@code copy.process.started} — proceso iniciado</li>
 *   <li>{@code copy.phase.transitioned} — transición de fase (inicio y fin)</li>
 *   <li>{@code copy.process.completed} — proceso completado</li>
 *   <li>{@code copy.process.failed} — proceso en error</li>
 *   <li>{@code copy.process.cancelled} — proceso cancelado</li>
 * </ul>
 *
 * <p>Eventos de módulo ({@code MODULO_*}) usan {@code copy.phase.transitioned}
 * porque representan progreso dentro de una fase.
 */
final class AmqpRoutingKeyMapper {

    static final String ROUTING_STARTED     = "copy.process.started";
    static final String ROUTING_TRANSITIONED = "copy.phase.transitioned";
    static final String ROUTING_COMPLETED   = "copy.process.completed";
    static final String ROUTING_FAILED      = "copy.process.failed";
    static final String ROUTING_CANCELLED   = "copy.process.cancelled";

    private AmqpRoutingKeyMapper() {
        // utilitaria — no instanciar
    }

    /**
     * Devuelve la routing key correspondiente al tipo de evento.
     *
     * @param tipo tipo de evento del proceso de copia
     * @return routing key AMQP
     */
    static String resolver(CopyEventType tipo) {
        return switch (tipo) {
            case PROCESO_COPIA_INICIADO -> ROUTING_STARTED;
            case PROCESO_COMPLETADO     -> ROUTING_COMPLETED;
            case PROCESO_ERROR          -> ROUTING_FAILED;
            case PROCESO_CANCELADO      -> ROUTING_CANCELLED;
            default                     -> ROUTING_TRANSITIONED;
        };
    }
}
