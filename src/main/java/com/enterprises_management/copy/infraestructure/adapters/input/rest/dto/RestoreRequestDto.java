package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de request para iniciar un proceso RESTORE desde un backup existente (REQ-RESTORE-01).
 *
 * @param backupRef      ruta relativa al archivo ZIP previamente generado
 * @param empresaDestino UUID de la empresa destino; en modo inplace es el UUID de la empresa a sobreescribir
 * @param inplace        si es true, elimina la empresa destino y la recrea desde el backup (H9)
 */
public record RestoreRequestDto(

        @NotBlank(message = "backupRef es obligatorio")
        String backupRef,

        @NotBlank(message = "empresaDestino es obligatorio")
        String empresaDestino,

        Boolean inplace

) {}
