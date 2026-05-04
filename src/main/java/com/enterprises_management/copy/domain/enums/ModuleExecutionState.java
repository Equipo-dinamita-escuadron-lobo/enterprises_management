package com.enterprises_management.copy.domain.enums;

/**
 * Estado de la ejecución de un módulo participante en la saga.
 * Define la máquina de estados de cada ejecución de módulo (PRD §8.2).
 */
public enum ModuleExecutionState {

    /** La ejecución fue creada pero aún no comenzó. */
    PENDIENTE,

    /** El módulo está siendo ejecutado actualmente. */
    EN_EJECUCION,

    /** El módulo terminó exitosamente. Estado terminal. */
    COMPLETADO,

    /** El módulo terminó con advertencias no bloqueantes. Estado terminal. */
    COMPLETADO_CON_ADVERTENCIAS,

    /** El módulo falló pero puede ser reintentado. No es terminal. */
    ERROR_REINTENTABLE,

    /** El módulo falló y agotó todos los reintentos. Estado terminal. */
    ERROR_NO_REINTENTABLE;

    /**
     * Indica si el estado es terminal (no se reintentará ni re-ejecutará).
     *
     * @return true si la ejecución ya no puede cambiar de estado
     */
    public boolean isTerminal() {
        return this == COMPLETADO
            || this == COMPLETADO_CON_ADVERTENCIAS
            || this == ERROR_NO_REINTENTABLE;
    }
}
