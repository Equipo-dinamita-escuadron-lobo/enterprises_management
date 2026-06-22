package com.enterprises_management.copy.domain.models;

import com.enterprises_management.copy.domain.enums.CopyEventType;

import java.time.LocalDateTime;

/**
 * Modelo de dominio de un evento del proceso de copia.
 * Sirve como log de trazabilidad y como fuente de eventos SSE (REQ-EVT-01, ADR-8).
 * El ID es BIGINT autoincremental en BD (secuencia para deduplicación SSE — ADR-8).
 */
public class CopyProcessEvent {

    private Long id;
    private final String idProceso;
    private final CopyEventType tipoEvento;
    private final LocalDateTime ocurridoEn;
    private final String payloadJson;

    public CopyProcessEvent(
            String idProceso,
            CopyEventType tipoEvento,
            String payloadJson
    ) {
        this.idProceso = idProceso;
        this.tipoEvento = tipoEvento;
        this.ocurridoEn = LocalDateTime.now();
        this.payloadJson = payloadJson;
    }

    /**
     * Factory method que crea un evento nuevo.
     * El ID BIGINT lo asigna la BD (autoincrement).
     *
     * @param idProceso   ID del proceso al que pertenece el evento
     * @param tipoEvento  tipo de evento (CopyEventType)
     * @param payloadJson payload adicional en JSON (puede ser null)
     * @return nuevo evento sin ID asignado (la BD lo asigna)
     */
    public static CopyProcessEvent crear(
            String idProceso,
            CopyEventType tipoEvento,
            String payloadJson
    ) {
        return new CopyProcessEvent(idProceso, tipoEvento, payloadJson);
    }

    // Getters

    public Long getId() { return id; }
    public String getIdProceso() { return idProceso; }
    public CopyEventType getTipoEvento() { return tipoEvento; }
    public LocalDateTime getOcurridoEn() { return ocurridoEn; }
    public String getPayloadJson() { return payloadJson; }

    // Setter del ID (asignado por la BD)

    public void setId(Long id) { this.id = id; }
}
