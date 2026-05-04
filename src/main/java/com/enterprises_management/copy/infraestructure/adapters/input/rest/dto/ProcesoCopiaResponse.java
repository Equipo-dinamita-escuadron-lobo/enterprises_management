package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import com.enterprises_management.copy.domain.enums.ProcessState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para el estado consolidado de un proceso de copia (REQ-API-03).
 * Incluye proceso + lista de fases.
 */
public record ProcesoCopiaResponse(
        String idProceso,
        CopyProcessType tipo,
        ProcessState estado,
        UUID empresaOrigen,
        String empresaDestino,
        String backupRef,
        LocalDateTime snapshotCorte,
        String iniciadoPor,
        int faseActual,
        LocalDateTime finalizadoEn,
        String errorResumen,
        List<FaseCopiaResponse> fases
) {}
