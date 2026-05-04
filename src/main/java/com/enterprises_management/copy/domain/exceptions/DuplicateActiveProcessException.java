package com.enterprises_management.copy.domain.exceptions;

import java.util.UUID;

/**
 * Excepción lanzada cuando ya existe un proceso activo (PENDIENTE o EN_PROCESO)
 * para la empresa origen indicada (REQ-PROC-01 — ADR-4).
 */
public class DuplicateActiveProcessException extends RuntimeException {

    /**
     * Crea la excepción con el UUID de la empresa origen que ya tiene proceso activo.
     *
     * @param empresaOrigen UUID de la empresa origen con proceso activo
     */
    public DuplicateActiveProcessException(UUID empresaOrigen) {
        super(String.format(
            "Ya existe un proceso activo para la empresa origen: %s", empresaOrigen
        ));
    }
}
