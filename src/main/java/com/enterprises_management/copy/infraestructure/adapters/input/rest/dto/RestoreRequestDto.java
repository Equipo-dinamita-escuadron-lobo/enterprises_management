package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de request para iniciar un proceso RESTORE desde un backup existente (REQ-RESTORE-01).
 *
 * @param backupRef     ruta relativa al archivo ZIP previamente generado
 * @param empresaDestino identificador de la empresa destino del RESTORE (diferente al origen)
 */
public record RestoreRequestDto(

        @NotBlank(message = "backupRef es obligatorio")
        String backupRef,

        @NotBlank(message = "empresaDestino es obligatorio")
        String empresaDestino

) {}
