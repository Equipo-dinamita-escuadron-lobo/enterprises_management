package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyPhaseConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para copy_phase_config.
 */
public interface CopyPhaseConfigJpaRepository extends JpaRepository<CopyPhaseConfigEntity, String> {

    /**
     * Busca los módulos activos para una fase, ordenados por campo orden ASC (REQ-CFG-01).
     *
     * @param numeroFase número de la fase (1-4)
     * @return lista de configuraciones activas ordenadas por orden
     */
    List<CopyPhaseConfigEntity> findAllByNumeroFaseAndActivoTrueOrderByOrdenAsc(Integer numeroFase);
}
