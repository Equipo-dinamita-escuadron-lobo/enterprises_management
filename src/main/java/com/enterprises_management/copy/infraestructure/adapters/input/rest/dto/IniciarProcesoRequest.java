package com.enterprises_management.copy.infraestructure.adapters.input.rest.dto;

import com.enterprises_management.copy.domain.enums.CopyProcessType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO de request para iniciar un proceso de copia (REQ-API-02).
 * <p>
 * Validaciones declarativas de negocio:
 * <ul>
 *   <li>RESTORE requiere backupRef no nulo/vacío (REQ-API-02) — validado con {@code @AssertTrue}</li>
 *   <li>DUPLICATE requiere empresaDestino (OQ-6)</li>
 * </ul>
 */
public record IniciarProcesoRequest(

        @NotNull(message = "tipo de proceso es requerido")
        CopyProcessType tipo,

        @NotNull(message = "empresaOrigen es requerido")
        UUID empresaOrigen,

        /** Requerido para DUPLICATE. OQ-6: nombre que se usará en Fase 1. */
        @Valid
        EmpresaDestinoDto empresaDestino,

        /** Requerido para RESTORE (REQ-API-02). */
        String backupRef,

        /**
         * Si true, genera un ZIP de backup al completar el proceso DUPLICATE (REQ-BACKUP-02).
         * Ignorado para tipos BACKUP (siempre genera) y RESTORE (nunca genera).
         */
        boolean generateBackup

) {

    /**
     * Valida que RESTORE siempre incluya backupRef (REQ-API-02).
     * Se evalúa solo cuando tipo está presente; si tipo es nulo la validación @NotNull lo captura primero.
     *
     * @return true si la combinación tipo/backupRef es válida
     */
    @AssertTrue(message = "backupRef es obligatorio para tipo=RESTORE")
    public boolean isBackupRefValidoParaRestore() {
        if (tipo == null) {
            // @NotNull en tipo ya captura este caso; no duplicar error
            return true;
        }
        return tipo != CopyProcessType.RESTORE || (backupRef != null && !backupRef.isBlank());
    }
}
