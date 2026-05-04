package com.enterprises_management.copy.infraestructure.adapters.output.amqp;

import java.time.LocalDateTime;

/**
 * DTO de evento para publicar en RabbitMQ (ADR-23, REQ-EVENT-01).
 *
 * <p>Shape estandarizado enviado al exchange {@code copy.process.exchange}:
 * {@code idProceso}, {@code tipo}, {@code ts} y {@code payload}.
 *
 * <p>Serializado por Jackson (ObjectMapper configurado en RabbitTemplate).
 */
public class EventoProcesoAmqpDto {

    /** Identificador del proceso de copia al que pertenece el evento. */
    private final String idProceso;

    /** Tipo del evento (por ejemplo, Proceso.completado). */
    private final String tipo;

    /** Marca de tiempo del evento. */
    private final LocalDateTime ts;

    /** Payload adicional con datos específicos del evento en JSON. */
    private final String payload;

    /** Secuencia del evento (BIGINT asignado por BD). */
    private final Long secuencia;

    public EventoProcesoAmqpDto(String idProceso, String tipo, LocalDateTime ts, String payload, Long secuencia) {
        this.idProceso = idProceso;
        this.tipo = tipo;
        this.ts = ts;
        this.payload = payload;
        this.secuencia = secuencia;
    }

    public String getIdProceso() { return idProceso; }
    public String getTipo() { return tipo; }
    public LocalDateTime getTs() { return ts; }
    public String getPayload() { return payload; }
    public Long getSecuencia() { return secuencia; }

    @Override
    public String toString() {
        return "EventoProcesoAmqpDto{" +
                "idProceso='" + idProceso + '\'' +
                ", tipo='" + tipo + '\'' +
                ", ts=" + ts +
                ", secuencia=" + secuencia +
                '}';
    }
}
