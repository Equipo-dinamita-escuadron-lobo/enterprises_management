package com.enterprises_management.copy.infraestructure.adapters.output.httpclient.dto;

/**
 * DTO canónico de response para el endpoint {@code POST /api/<modulo>/copy/{idProceso}/cancel}.
 *
 * <p>El participante confirma la cancelación retornando el estado resultante (REQ-CONTRACT-01, ADR-18).
 *
 * @param estado estado tras la cancelación — normalmente {@code CANCELADO}
 */
public record CopyCancelResponseDto(String estado) {}
