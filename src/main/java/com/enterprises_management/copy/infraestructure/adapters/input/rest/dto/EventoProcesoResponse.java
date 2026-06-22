package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import com.enterprises_management.copy.domain.enums.CopyEventType;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para un evento del proceso de copia (REQ-EVT-01).
 */
public record EventoProcesoResponse(
        Long id,
        String idProceso,
        CopyEventType tipoEvento,
        LocalDateTime ocurridoEn,
        String payloadJson
) {}
