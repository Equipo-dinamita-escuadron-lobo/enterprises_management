package com.enterprises_management.copy.domain.enums;

/**
 * Estado de una fase de la saga de copia.
 * Define la máquina de estados de cada fase (PRD §8.2).
 */
public enum PhaseState {

    /** La fase fue creada pero aún no ha comenzado. */
    PENDIENTE,

    /** La fase está ejecutando sus módulos activamente. */
    EN_PROCESO,

    /** Todos los módulos de la fase terminaron exitosamente. Estado terminal. */
    COMPLETADA,

    /** Al menos un módulo agotó reintentos. Estado terminal. */
    ERROR,

    /** La fase fue omitida por configuración. Estado terminal. */
    OMITIDA;

    /**
     * Indica si el estado es terminal (no hay más transiciones posibles).
     *
     * @return true si la fase ya no puede cambiar de estado
     */
    public boolean isTerminal() {
        return this == COMPLETADA || this == ERROR || this == OMITIDA;
    }
}
