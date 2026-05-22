package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

import java.util.List;

/**
 * DTO canónico de response para el endpoint {@code POST /api/<modulo>/copy/phase}.
 *
 * <p>El orquestador interpreta el campo {@code estado} para decidir reintentos (REQ-CONTRACT-03, ADR-18).
 *
 * <p>Valores válidos de {@code estado}:
 * <ul>
 *   <li>{@code COMPLETADO} — éxito limpio</li>
 *   <li>{@code COMPLETADO_CON_ADVERTENCIAS} — éxito con advertencias no críticas</li>
 *   <li>{@code ERROR_REINTENTABLE} — fallo transitorio, el orquestador puede reintentar</li>
 *   <li>{@code ERROR_NO_REINTENTABLE} — fallo estructural, no reintentar</li>
 * </ul>
 *
 * @param estado                 resultado de la ejecución de la fase
 * @param registrosProcesados    cantidad de entidades procesadas
 * @param equivalenciasGeneradas equivalencias de IDs generadas en esta fase
 * @param mensaje                descripción del resultado o error
 * @param advertencias           lista de advertencias no críticas (puede estar vacía)
 * @param datosExportados        datos exportados por el participante en modo BACKUP (null si no aplica)
 */
public record CopyPhaseResponseDto(
        String estado,
        int registrosProcesados,
        List<CopyEquivalenceDto> equivalenciasGeneradas,
        String mensaje,
        List<String> advertencias,
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        Object datosExportados
) {}
