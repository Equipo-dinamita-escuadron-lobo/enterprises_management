package com.enterprises_management.copy.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta una transición de estado no permitida
 * en la máquina de estados del orquestador de copia (ADR-7).
 */
public class InvalidPhaseTransitionException extends RuntimeException {

    /**
     * Crea la excepción con los estados de origen y destino inválidos.
     *
     * @param from estado de origen (name() del enum)
     * @param to   estado de destino (name() del enum)
     */
    public InvalidPhaseTransitionException(String from, String to) {
        super(String.format(
            "Transición de estado inválida: %s → %s", from, to
        ));
    }
}
