package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyProcessEvent;

import java.util.List;

/**
 * Puerto de salida para persistir y consultar eventos del proceso de copia.
 * Soporta REQ-EVT-01 y trazabilidad SSE (ADR-8).
 */
public interface IProcessEventRepositoryPort {

    /**
     * Persiste un evento nuevo. El ID BIGINT lo asigna la BD.
     *
     * @param evento evento a registrar
     * @return evento guardado con ID asignado por la BD
     */
    CopyProcessEvent guardar(CopyProcessEvent evento);

    /**
     * Retorna todos los eventos de un proceso ordenados por ID ascendente
     * (secuencia de ocurrencia — ADR-8 deduplicación SSE).
     *
     * @param idProceso ID del proceso
     * @return lista de eventos en orden cronológico
     */
    List<CopyProcessEvent> buscarPorProceso(String idProceso);
}
