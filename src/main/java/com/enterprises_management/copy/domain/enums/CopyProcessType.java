package com.enterprises_management.copy.domain.enums;

/**
 * Tipo del proceso de copia.
 * Define el modo de operación del orquestador (PRD §API, REQ-API-02).
 */
public enum CopyProcessType {

    /** Copia de seguridad de una empresa existente. */
    BACKUP,

    /** Restauración de una empresa desde un backup de referencia. */
    RESTORE,

    /** Duplicación de una empresa hacia una nueva empresa destino. */
    DUPLICATE
}
