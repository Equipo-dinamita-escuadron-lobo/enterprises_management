package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;

/**
 * Puerto de salida para la búsqueda y consulta de direcciones geográficas.
 * Define las operaciones necesarias para obtener información sobre países,
 * departamentos y ciudades asociadas desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IAddressSearchOutputPort {

    /**
     * Recupera todos los países disponibles en el sistema.
     *
     * @return List<Country> Lista de todos los países registrados
     * @see Country
     */
    List<Country> getAllCountries();

    /**
     * Recupera todos los departamentos asociados a un país.
     *
     * @param idCountry Identificador único del país
     * @return List<Department> Lista de departamentos asociados al país
     * @see Department
     */
    List<Department> getAllDepartments(Long idCountry);

    /**
     * Obtiene la información de un departamento específico incluyendo sus ciudades.
     *
     * @param idDepartment Identificador único del departamento
     * @return Department Objeto que contiene la información del departamento y sus ciudades
     * @see Department
     */
    Department getAllCities(Long idDepartment);
}
