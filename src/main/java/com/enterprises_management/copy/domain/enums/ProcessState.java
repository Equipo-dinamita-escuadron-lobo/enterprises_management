package com.enterprises_management.copy.domain.enums;

/**
 * Estado del proceso raíz de la saga de copia.
 * Define la máquina de estados del proceso (PRD §8.2).
 */
public enum ProcessState {

    /** El proceso fue creado pero aún no comenzó su ejecución. */
    PENDIENTE,

    /** El proceso está siendo ejecutado activamente. */
    EN_PROCESO,

    /** Todas las fases terminaron exitosamente. Estado terminal. */
    COMPLETADO,

    /** Una fase falló y agotó los reintentos. Estado terminal. */
    ERROR,

    /** El proceso fue cancelado explícitamente. Estado terminal. */
    CANCELADO;

    /**
     * Indica si el estado es terminal (no hay más transiciones posibles).
     *
     * @return true si el proceso ya no puede cambiar de estado
     */
    public boolean isTerminal() {
        return this == COMPLETADO || this == ERROR || this == CANCELADO;
    }
}
