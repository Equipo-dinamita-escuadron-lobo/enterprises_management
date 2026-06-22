package com.enterprises_management.copy.application.output;

import com.enterprises_management.copy.domain.models.CopyPhaseConfig;

import java.util.List;

/**
 * Puerto de salida para leer la configuración de módulos por fase.
 * Soporta REQ-CFG-01: módulos configurables sin cambiar código.
 */
public interface IPhaseConfigRepositoryPort {

    /**
     * Retorna los módulos activos para una fase, ordenados por su campo 'orden'.
     *
     * @param numeroFase número de la fase (1-4)
     * @return lista de configuraciones activas, ordenadas por orden ASC
     */
    List<CopyPhaseConfig> buscarActivosPorFase(int numeroFase);
}
