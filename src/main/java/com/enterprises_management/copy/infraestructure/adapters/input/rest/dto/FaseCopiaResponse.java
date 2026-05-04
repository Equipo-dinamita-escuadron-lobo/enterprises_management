package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import com.enterprises_management.copy.domain.enums.PhaseState;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para una fase de copia dentro del estado consolidado (REQ-API-03).
 */
public record FaseCopiaResponse(
        String id,
        String idProceso,
        int numero,
        String nombre,
        PhaseState estado,
        LocalDateTime iniciadaEn,
        LocalDateTime finalizadaEn,
        String errorDetalle
) {}
