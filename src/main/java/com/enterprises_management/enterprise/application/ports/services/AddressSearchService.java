package com.enterprises_management.enterprise.application.ports.services;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ICityRepository;
import com.enterprises_management.enterprise.application.ports.output.ICountryRepository;
import com.enterprises_management.enterprise.application.ports.output.IDepartmentRepository;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Servicio que implementa las operaciones de búsqueda de direcciones.
 * Gestiona la lógica de negocio para la obtención de países, departamentos y ciudades,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class AddressSearchService implements IAddressSearchManagerPort {

    private final ICountryRepository countryRepository;
    private final IDepartmentRepository departmentRepository;
    private final ICityRepository cityRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Department> getAllDepartments(Long idCountry) {
        return departmentRepository.findByCountryId(idCountry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getAllCities(Long idDepartment) {
        return cityRepository.findByDepartmentId(idDepartment);
    }
}
