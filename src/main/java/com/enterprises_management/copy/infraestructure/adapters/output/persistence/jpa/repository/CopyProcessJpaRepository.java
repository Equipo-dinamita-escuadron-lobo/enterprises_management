package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.domain.enums.ProcessState;
import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Repositorio JPA para copy_process.
 * Incluye query method para ADR-4: verificación aplicativa de proceso activo.
 */
public interface CopyProcessJpaRepository extends JpaRepository<CopyProcessEntity, String> {

    /**
     * Verifica si existe un proceso activo (PENDIENTE o EN_PROCESO) para una empresa origen.
     * Implementa ADR-4: el índice único parcial existe en Postgres; en H2 la verificación es aplicativa.
     *
     * @param empresaOrigen UUID de la empresa origen como String
     * @param estados       colección de estados activos (PENDIENTE, EN_PROCESO)
     * @return true si existe al menos un proceso con esos estados para la empresa
     */
    boolean existsByEmpresaOrigenAndEstadoIn(String empresaOrigen, Collection<ProcessState> estados);
}
