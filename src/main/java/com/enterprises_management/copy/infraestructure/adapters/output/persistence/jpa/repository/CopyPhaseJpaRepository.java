package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyPhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para copy_phase.
 */
public interface CopyPhaseJpaRepository extends JpaRepository<CopyPhaseEntity, String> {

    /**
     * Busca las fases de un proceso, ordenadas por número ascendente.
     *
     * @param idProceso ID del proceso padre
     * @return lista de fases ordenadas por numero ASC
     */
    List<CopyPhaseEntity> findAllByIdProcesoOrderByNumeroAsc(String idProceso);

    /**
     * Busca una fase específica por proceso y número.
     *
     * @param idProceso  ID del proceso padre
     * @param numero     número de la fase (1-4)
     * @return fase encontrada o vacío
     */
    Optional<CopyPhaseEntity> findByIdProcesoAndNumero(String idProceso, Integer numero);
}
