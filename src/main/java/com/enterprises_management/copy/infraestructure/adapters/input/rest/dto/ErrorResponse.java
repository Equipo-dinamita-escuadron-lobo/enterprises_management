package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta de error estándar (ADR-10).
 * Incluye código de error del bounded context copy (COPY_*), mensaje y timestamp.
 */
public record ErrorResponse(
        String codigo,
        String mensaje,
        LocalDateTime timestamp,
        String correlationId,
        List<CampoErrorResponse> erroresCampos
) {
    /** Constructor sin errores de campo (para errores no-400). */
    public ErrorResponse(String codigo, String mensaje, String correlationId) {
        this(codigo, mensaje, LocalDateTime.now(), correlationId, null);
    }

    /** Registro para detalles de validación de campos (400). */
    public record CampoErrorResponse(String campo, String mensaje) {}
}
