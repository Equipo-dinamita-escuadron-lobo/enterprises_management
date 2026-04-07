package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;

/**
 * Puerto de entrada para la gestión y búsqueda de direcciones geográficas.
 * Define las operaciones disponibles para la consulta de departamentos y sus
 * ciudades asociadas.
 *
 * @author [Juan Camilo Zarta Campo]
 * @version 1.0
 * @since 1.0.0
 */
public interface IAddressSearchManagerPort {

    /**
     * Recupera el listado completo de países disponibles en el sistema.
     *
     * @return List<Country> Colección de países registrados en el sistema
     * @see Country
     */
    List<Country> getAllCountries();

    /**
     * Recupera el listado completo de los departamentos por id del pais disponibles
     * en el sistema.
     *
     * @return List<Department> Colección de departamentos por id del pais
     *         registrados en el sistema
     * @see Department
     */
    List<Department> getAllDepartments(Long idCountry);

    /**
     * Recupera el listado completo de las ciudades por id del departamento
     * disponibles en el sistema.
     *
     * @param idDepartment Identificador único del departamento a consultar
     * @return List<City> Colección de ciudades por id del departamento registradas
     *         en el sistema
     * @see City
     * @see Department
     */
    List<City> getAllCities(Long idDepartment);
}
