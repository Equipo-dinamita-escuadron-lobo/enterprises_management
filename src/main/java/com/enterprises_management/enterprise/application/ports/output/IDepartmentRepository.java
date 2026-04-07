package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Puerto de salida para el acceso a datos de departamentos.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Repository
public interface IDepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Encuentra todos los departamentos por ID de país.
     *
     * @param countryId ID del país
     * @return Lista de departamentos
     */
    List<Department> findByCountryId(Long countryId);
}