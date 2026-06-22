package com.enterprises_management.copy.domain.enums;

/**
 * Tipos de eventos del proceso de copia registrados en copy_process_event.
 * Usados también para el payload SSE (REQ-EVT-01, ADR-8).
 */
public enum CopyEventType {

    /** El proceso fue creado e iniciado. */
    PROCESO_COPIA_INICIADO("ProcesoCopia.iniciado"),

    /** Una fase comenzó su ejecución. */
    FASE_INICIADA("Fase.iniciada"),

    /** Se invocó a un módulo participante. */
    MODULO_INICIADO("Modulo.iniciado"),

    /** El módulo respondió exitosamente. */
    MODULO_COMPLETADO("Modulo.completado"),

    /** El módulo respondió con error. */
    MODULO_ERROR("Modulo.error"),

    /** La fase terminó con todos sus módulos exitosos. */
    FASE_COMPLETADA("Fase.completada"),

    /** La fase terminó con error no reintentable. */
    FASE_ERROR("Fase.error"),

    /** El proceso terminó exitosamente. */
    PROCESO_COMPLETADO("Proceso.completado"),

    /** El proceso terminó con error. */
    PROCESO_ERROR("Proceso.error"),

    /** El proceso fue cancelado explícitamente. */
    PROCESO_CANCELADO("Proceso.cancelado");

    /** Nombre del tipo de evento tal como aparece en SSE y en la base de datos. */
    private final String tipo;

    CopyEventType(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Retorna el nombre del tipo de evento para uso en SSE y en la BD.
     *
     * @return el nombre del evento en formato {Entidad}.{accion}
     */
    public String getTipo() {
        return tipo;
    }
}
