package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO canónico de request para el endpoint {@code POST /api/<modulo>/copy/phase}.
 *
 * <p>Todos los campos son obligatorios. El participante DEBE rechazar con HTTP 400
 * si alguno falta (REQ-CONTRACT-02, ADR-18).
 *
 * @param idProceso        identificador único del proceso de copia
 * @param fase             número de fase que se ejecuta (≥ 1)
 * @param entOrigen        ID de la empresa origen
 * @param entDestino       ID de la empresa destino
 * @param snapshotCorte    timestamp de corte — se copian entidades creadas antes o en este instante
 * @param equivalenciasPrev equivalencias generadas por fases anteriores para remapeo de FKs
 */
public record CopyPhaseRequestDto(
        UUID idProceso,
        int fase,
        String entOrigen,
        String entDestino,
        Instant snapshotCorte,
        List<CopyEquivalenceDto> equivalenciasPrev
) {}
