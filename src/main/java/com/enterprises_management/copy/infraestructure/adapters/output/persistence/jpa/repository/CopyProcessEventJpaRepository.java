package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyProcessEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para copy_process_event.
 * Eventos ordenados por id ASC para deduplicación SSE (ADR-8).
 */
public interface CopyProcessEventJpaRepository extends JpaRepository<CopyProcessEventEntity, Long> {

    /**
     * Busca todos los eventos de un proceso en orden cronológico (id ASC = secuencia ADR-8).
     *
     * @param idProceso ID del proceso
     * @return lista de eventos ordenados por id ASC
     */
    List<CopyProcessEventEntity> findAllByIdProcesoOrderByIdAsc(String idProceso);
}
