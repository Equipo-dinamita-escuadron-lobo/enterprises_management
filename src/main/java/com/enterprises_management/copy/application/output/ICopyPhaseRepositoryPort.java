package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyPhase;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir y recuperar fases de un proceso de copia.
 */
public interface ICopyPhaseRepositoryPort {

    /**
     * Persiste una fase nueva.
     *
     * @param fase fase a guardar
     * @return fase guardada
     */
    CopyPhase guardar(CopyPhase fase);

    /**
     * Actualiza una fase existente.
     *
     * @param fase fase con los cambios aplicados
     * @return fase actualizada
     */
    CopyPhase actualizar(CopyPhase fase);

    /**
     * Busca las fases de un proceso ordenadas por número (1-4).
     *
     * @param idProceso ID del proceso padre
     * @return lista de fases en orden ascendente de número
     */
    List<CopyPhase> buscarPorProceso(String idProceso);

    /**
     * Busca una fase específica de un proceso por su número.
     *
     * @param idProceso   ID del proceso padre
     * @param numeroFase  número de la fase (1-4)
     * @return fase encontrada o vacío
     */
    Optional<CopyPhase> buscarPorProcesoYNumero(String idProceso, int numeroFase);
}
