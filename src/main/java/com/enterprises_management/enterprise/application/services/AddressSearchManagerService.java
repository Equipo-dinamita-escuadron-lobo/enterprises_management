package com.enterprises_management.enterprise.application.services;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ICityRepository;
import com.enterprises_management.enterprise.application.ports.output.ICountryRepository;
import com.enterprises_management.enterprise.application.ports.output.IDepartmentRepository;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio que implementa la lógica de negocio para la búsqueda de direcciones.
 * Maneja las operaciones de consulta de países, departamentos y ciudades.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class AddressSearchManagerService implements IAddressSearchManagerPort {

    private final ICountryRepository countryRepository;
    private final IDepartmentRepository departmentRepository;
    private final ICityRepository cityRepository;

    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    @Override
    public List<Department> getAllDepartments(Long idCountry) {
        return departmentRepository.findByCountryId(idCountry);
    }

    @Override
    public List<City> getAllCities(Long idDepartment) {
        return cityRepository.findByDepartmentId(idDepartment);
    }
}