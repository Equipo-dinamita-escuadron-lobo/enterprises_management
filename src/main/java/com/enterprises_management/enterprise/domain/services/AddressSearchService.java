package com.enterprises_management.enterprise.domain.services;

import com.enterprises_management.enterprise.application.ports.input.IAddressSearchManagerPort;
import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressSearchService implements IAddressSearchManagerPort {

    @Override
    public List<Department> getDepartment(Country country) {
        return null;
    }

    @Override
    public List<City> getCities(Department department) {
        return null;
    }
}
