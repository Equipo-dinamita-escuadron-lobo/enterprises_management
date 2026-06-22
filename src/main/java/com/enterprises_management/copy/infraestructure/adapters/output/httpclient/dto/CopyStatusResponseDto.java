package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

/**
 * DTO canónico de response para el endpoint {@code GET /api/<modulo>/copy/{idProceso}/status}.
 *
 * <p>Permite al orquestador consultar el estado local del proceso en el participante
 * sin re-ejecutar la fase (REQ-CONTRACT-01, ADR-18).
 *
 * @param fase                número de fase reportado por el participante
 * @param estado              estado actual en el participante
 * @param registrosProcesados cantidad de registros ya procesados
 * @param intentos            número de intentos de ejecución realizados
 * @param ultimoError         detalle del último error, o null si no hubo error
 */
public record CopyStatusResponseDto(
        int fase,
        String estado,
        int registrosProcesados,
        int intentos,
        String ultimoError
) {}
