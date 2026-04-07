package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Puerto de salida para el acceso a datos de ciudades.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Repository
public interface ICityRepository extends JpaRepository<City, Long> {

    /**
     * Encuentra todas las ciudades por ID de departamento.
     *
     * @param departmentId ID del departamento
     * @return Lista de ciudades
     */
    List<City> findByDepartmentId(Long departmentId);
}