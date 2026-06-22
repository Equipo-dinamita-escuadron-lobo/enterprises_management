package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyModuleExecution;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y recuperar ejecuciones de módulos.
 */
public interface IModuleExecutionRepositoryPort {

    /**
     * Persiste una ejecución de módulo nueva.
     *
     * @param ejecucion ejecución a guardar
     * @return ejecución guardada
     */
    CopyModuleExecution guardar(CopyModuleExecution ejecucion);

    /**
     * Actualiza una ejecución de módulo existente.
     *
     * @param ejecucion ejecución con los cambios aplicados
     * @return ejecución actualizada
     */
    CopyModuleExecution actualizar(CopyModuleExecution ejecucion);

    /**
     * Busca las ejecuciones de módulos para una fase específica.
     *
     * @param idFase ID de la fase
     * @return lista de ejecuciones de esa fase
     */
    List<CopyModuleExecution> buscarPorFase(String idFase);

    /**
     * Busca una ejecución específica por clave de idempotencia (REQ-IDEM-01).
     *
     * @param idProceso ID del proceso
     * @param idFase    ID de la fase
     * @param modulo    nombre del módulo
     * @return ejecución encontrada o vacío
     */
    Optional<CopyModuleExecution> buscarPorClaveIdempotencia(String idProceso, String idFase, String modulo);
}
