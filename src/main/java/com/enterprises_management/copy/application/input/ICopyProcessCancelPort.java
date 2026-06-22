package com.enterprises_management.copy.application.input;

/**
 * Puerto de entrada para cancelar un proceso de copia activo.
 * Corresponde a POST /api/enterprises/copy/processes/{id}/cancel (REQ-PROC-01).
 */
public interface ICopyProcessCancelPort {

    /**
     * Cancela un proceso de copia que se encuentre en estado PENDIENTE o EN_PROCESO.
     *
     * @param idProceso    ID del proceso a cancelar
     * @param canceladoPor claim 'sub' del JWT del usuario que cancela
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessCancelDeniedException
     *         si el proceso ya está en estado terminal (COMPLETADO, ERROR, CANCELADO)
     */
    void cancelar(String idProceso, String canceladoPor);
}
