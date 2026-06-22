package com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.repository;

import com.enterprises_management.copy.infraestructure.adapters.output.persistence.jpa.entity.CopyEquivalenceIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para copy_equivalence_id.
 * Incluye consulta con filtros opcionales para REQ-EQ-02.
 */
public interface CopyEquivalenceIdJpaRepository extends JpaRepository<CopyEquivalenceIdEntity, String> {

    /**
     * Busca una equivalencia por su clave de negocio (id_proceso, modulo, tabla, id_viejo).
     *
     * @param idProceso ID del proceso
     * @param modulo    nombre del módulo
     * @param tabla     nombre de la tabla
     * @param idViejo   ID original
     * @return equivalencia encontrada o vacío
     */
    Optional<CopyEquivalenceIdEntity> findByIdProcesoAndModuloAndTablaAndIdViejo(
            String idProceso, String modulo, String tabla, String idViejo);

    /**
     * Consulta filtrable con parámetros opcionales (REQ-EQ-02).
     * Los parámetros null no aplican como filtro.
     *
     * @param idProceso ID del proceso (requerido)
     * @param modulo    filtro por módulo (opcional, null = sin filtro)
     * @param tabla     filtro por tabla (opcional, null = sin filtro)
     * @param idViejo   filtro por idViejo (opcional, null = sin filtro)
     * @return lista de equivalencias que coinciden
     */
    @Query("SELECT e FROM CopyEquivalenceIdEntity e WHERE e.idProceso = :idProceso " +
           "AND (:modulo IS NULL OR e.modulo = :modulo) " +
           "AND (:tabla IS NULL OR e.tabla = :tabla) " +
           "AND (:idViejo IS NULL OR e.idViejo = :idViejo)")
    List<CopyEquivalenceIdEntity> findByFilters(
            @Param("idProceso") String idProceso,
            @Param("modulo") String modulo,
            @Param("tabla") String tabla,
            @Param("idViejo") String idViejo);
}
