package com.enterprises_management.copy.application.input;

import com.enterprises_management.copy.domain.models.CopyPhase;
import com.enterprises_management.copy.domain.models.CopyProcess;
import com.enterprises_management.copy.domain.models.CopyProcessEvent;

import java.util.List;

/**
 * Puerto de entrada para consultar el estado de un proceso de copia.
 * Corresponde a GET /api/enterprises/copy/processes/{id} y
 * GET /api/enterprises/copy/processes/{id}/events (REQ-API-03, REQ-PROC-01).
 */
public interface ICopyProcessQueryPort {

    /**
     * Consulta el estado completo de un proceso.
     *
     * @param idProceso ID del proceso a consultar
     * @return proceso con su información completa
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     */
    CopyProcess consultarProceso(String idProceso);

    /**
     * Consulta las fases de un proceso.
     *
     * @param idProceso ID del proceso
     * @return lista de fases ordenadas por número (1-4)
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     */
    List<CopyPhase> consultarFases(String idProceso);

    /**
     * Consulta el log de eventos de un proceso.
     *
     * @param idProceso ID del proceso
     * @return lista de eventos ordenados por ID (secuencia de ocurrencia)
     * @throws com.enterprises_management.copy.domain.exceptions.CopyProcessNotFoundException
     *         si el proceso no existe
     */
    List<CopyProcessEvent> consultarEventos(String idProceso);

    /**
     * Lista todos los procesos de copia ordenados por fecha de inicio descendente.
     *
     * @return lista de todos los procesos
     */
    List<CopyProcess> listarProcesos();
}
