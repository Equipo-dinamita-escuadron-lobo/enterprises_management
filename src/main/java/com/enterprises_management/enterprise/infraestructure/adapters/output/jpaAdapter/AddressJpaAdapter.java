package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import com.enterprises_management.enterprise.application.ports.output.IAddressSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ICitiesbyDepartmentMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ICountriesMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IDepartmentsMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IAddressRepository;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IDepartmentAddressRepository;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Adaptador de salida para la consulta de direcciones mediante JPA.
 *
 * <p>
 * Esta clase implementa el puerto de salida {@link IAddressSearchOutputPort}
 * y se encarga de recuperar la información de países, departamentos y ciudades
 * desde la capa de persistencia, transformando las entidades JPA a modelos de
 * dominio.
 * </p>
 *
 * <p>
 * Responsabilidades:
 * </p>
 * <ul>
 * <li>Obtener todos los países</li>
 * <li>Obtener todos los departamentos asociados a un país</li>
 * <li>Obtener un departamento con sus ciudades asociadas</li>
 * </ul>
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Component
@Data
@RequiredArgsConstructor
public class AddressJpaAdapter implements IAddressSearchOutputPort {

    /**
     * Repositorio general de direcciones.
     */
    private final IAddressRepository addressRepository;

    /**
     * Mapper para conversión de entidades de país a modelo de dominio.
     */
    private final ICountriesMapper countriesMapper;

    /**
     * Repositorio de departamentos.
     */
    private final IDepartmentAddressRepository departmentAddressRepository;

    /**
     * Mapper para conversión de entidades de departamento con ciudades
     * a modelo de dominio.
     */
    private final ICitiesbyDepartmentMapper citiesbyDepartmentMapper;

    /**
     * Mapper para conversión de entidades de departamento a modelo de dominio.
     */
    private final IDepartmentsMapper departmentsMapper;

    /**
     * Obtiene todos los países registrados en el sistema.
     *
     * @return lista de países en modelo de dominio
     */
    @Override
    public List<Country> getAllCountries() {
        return countriesMapper.toModelList(addressRepository.findAll());
    }

    /**
     * Obtiene todos los departamentos asociados a un país específico.
     *
     * @param idCountry identificador del país
     * @return lista de departamentos asociados al país indicado
     */
    @Override
    public List<Department> getAllDepartments(@NonNull Long idCountry) {
        return departmentsMapper.toModelList(
                departmentAddressRepository.findByCountry_Id(idCountry));
    }

    /**
     * Obtiene la información de un departamento con sus ciudades asociadas.
     *
     * @param idDepartment identificador del departamento
     * @return departamento con sus ciudades asociadas
     * @throws IllegalArgumentException si no existe un departamento con el id
     *                                  indicado
     */
    @Override
    public Department getAllCities(@NonNull Long idDepartment) {
        return departmentAddressRepository.findById(idDepartment)
                .map(citiesbyDepartmentMapper::toDomain)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el departamento con id: " + idDepartment));
    }
}