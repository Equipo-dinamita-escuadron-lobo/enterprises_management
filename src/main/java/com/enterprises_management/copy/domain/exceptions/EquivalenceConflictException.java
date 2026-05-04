package com.enterprises_management.copy.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta registrar una equivalencia cuya clave
 * (idProceso, modulo, tabla, idViejo) ya existe con un idNuevo diferente (ADR-9).
 */
public class EquivalenceConflictException extends RuntimeException {

    /**
     * Crea la excepción con el contexto del conflicto de equivalencia.
     *
     * @param idProceso ID del proceso
     * @param modulo    nombre del módulo
     * @param tabla     nombre de la tabla
     * @param idViejo   ID original en conflicto
     */
    public EquivalenceConflictException(
            String idProceso,
            String modulo,
            String tabla,
            String idViejo
    ) {
        super(String.format(
            "Conflicto de equivalencia: clave (%s, %s, %s, %s) ya existe con un idNuevo diferente",
            idProceso, modulo, tabla, idViejo
        ));
    }
}
