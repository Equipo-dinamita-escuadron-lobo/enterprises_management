package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.projection.IEnterpriseInfoProjection;

/**
 * Repositorio JPA para la entidad EnterpriseEntity.
 * Proporciona métodos para realizar operaciones CRUD en la base de datos.
 */
public interface IEnterpriseRepository extends JpaRepository<EnterpriseEntity, UUID> {

    /**
     * Obtiene información básica de las empresas activas (state = 0).
     *
     * @return lista de proyecciones con información básica de empresas activas
     */
    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state FROM EnterpriseEntity e WHERE e.state = 0")
    List<IEnterpriseInfoProjection> findEnterpriseInfo();

    /**
     * Verifica si existe una empresa con el NIT especificado.
     *
     * @param nit el NIT de la empresa
     * @return true si existe una empresa con el NIT, false en caso contrario
     */
    boolean existsByNit(String nit);

    /**
     * Obtiene información básica de las empresas inactivas (state = 1).
     *
     * @return lista de proyecciones con información básica de empresas inactivas
     */
    @Query("SELECT e.id AS id, e.name AS name, e.nit AS nit, e.logo AS logo, e.state AS state FROM EnterpriseEntity e WHERE e.state = 1")
    List<IEnterpriseInfoProjection> findEnterpriseInfoInactive();

    /**
     * Obtiene una empresa por el código de la materia asociada.
     *
     * @param subjectCode el código de la materia asociada a la empresa
     * @return la entidad de la empresa, o null si no se encuentra
     */
    List<EnterpriseEntity> findBySubjectsCode(String subjectCode);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM enterprise_tax_liabilities WHERE enterprise_id = :enterpriseId", nativeQuery = true)
    void deleteTaxLiabilitiesByEnterpriseId(@Param("enterpriseId") UUID enterpriseId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO enterprise_tax_liabilities (enterprise_id, tax_liability_id) VALUES (:enterpriseId, :taxId)", nativeQuery = true)
    void insertTaxLiability(@Param("enterpriseId") UUID enterpriseId, @Param("taxId") Long taxId);

    @Query("SELECT DISTINCT e FROM EnterpriseEntity e LEFT JOIN e.subjects s " +
           "WHERE e.state = 0 AND (" +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.nit)  LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<EnterpriseEntity> searchByNameNitOrSubject(@Param("q") String q);
}
