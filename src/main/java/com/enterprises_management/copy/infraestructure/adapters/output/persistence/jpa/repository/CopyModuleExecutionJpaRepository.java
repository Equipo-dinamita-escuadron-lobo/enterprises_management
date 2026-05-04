package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyModuleExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para copy_module_execution.
 * Incluye métodos para la clave de idempotencia (REQ-IDEM-01).
 */
public interface CopyModuleExecutionJpaRepository extends JpaRepository<CopyModuleExecutionEntity, String> {

    /**
     * Busca las ejecuciones de módulos para una fase específica.
     *
     * @param idFase ID de la fase
     * @return lista de ejecuciones de esa fase
     */
    List<CopyModuleExecutionEntity> findAllByIdFase(String idFase);

    /**
     * Busca la ejecución por clave de idempotencia (id_proceso, id_fase, modulo).
     *
     * @param idProceso ID del proceso
     * @param idFase    ID de la fase
     * @param modulo    nombre del módulo
     * @return ejecución encontrada o vacío
     */
    Optional<CopyModuleExecutionEntity> findByIdProcesoAndIdFaseAndModulo(
            String idProceso, String idFase, String modulo);

    /**
     * Verifica existencia por clave de idempotencia.
     *
     * @param idProceso ID del proceso
     * @param idFase    ID de la fase
     * @param modulo    nombre del módulo
     * @return true si ya existe la ejecución
     */
    boolean existsByIdProcesoAndIdFaseAndModulo(String idProceso, String idFase, String modulo);
}
