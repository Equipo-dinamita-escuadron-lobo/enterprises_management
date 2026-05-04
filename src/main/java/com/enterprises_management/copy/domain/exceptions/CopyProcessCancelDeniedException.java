package com.enterprises_management.copy.domain.exceptions;

import com.enterprises_management.copy.domain.enums.ProcessState;

/**
 * Excepción lanzada cuando se intenta cancelar un proceso que ya está
 * en un estado terminal y no admite cancelación.
 */
public class CopyProcessCancelDeniedException extends RuntimeException {

    /**
     * Crea la excepción con el ID del proceso y su estado actual.
     *
     * @param id     ID del proceso que no se puede cancelar
     * @param estado estado actual del proceso (debe ser terminal)
     */
    public CopyProcessCancelDeniedException(String id, ProcessState estado) {
        super(String.format(
            "No se puede cancelar el proceso %s en estado %s", id, estado.name()
        ));
    }
}
