package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyProcessEvent;

/**
 * Puerto de salida para notificar eventos a suscriptores SSE.
 * Desacopla el SagaEngineService de la implementación concreta de SSE (ADR-8).
 */
public interface IProcessNotifierPort {

    /**
     * Publica un evento hacia todos los suscriptores activos del proceso.
     *
     * @param evento evento a publicar
     */
    void publicar(CopyProcessEvent evento);
}
