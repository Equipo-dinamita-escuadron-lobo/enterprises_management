package com.enterprises_management.copy.domain.exceptions;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra un proceso de copia por su ID.
 */
public class CopyProcessNotFoundException extends RuntimeException {

    /**
     * Crea la excepción con el ID del proceso no encontrado.
     *
     * @param id UUID del proceso que no existe
     */
    public CopyProcessNotFoundException(String id) {
        super(String.format("Proceso de copia no encontrado: %s", id));
    }
}
