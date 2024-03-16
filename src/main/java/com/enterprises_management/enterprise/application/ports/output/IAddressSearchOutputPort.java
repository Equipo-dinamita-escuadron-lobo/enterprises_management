package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;

import java.util.List;

public interface IAddressSearchOutputPort {

    List<Department> getAllDepartment();
    public Department getAllCities(Long idDepartment);
}
