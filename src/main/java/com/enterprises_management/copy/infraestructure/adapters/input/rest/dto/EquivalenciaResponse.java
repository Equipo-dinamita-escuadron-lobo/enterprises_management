package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una equivalencia de IDs (REQ-EQ-02).
 */
public record EquivalenciaResponse(
        String id,
        String idProceso,
        String modulo,
        String tabla,
        String idViejo,
        String idNuevo,
        LocalDateTime registradoEn
) {}
