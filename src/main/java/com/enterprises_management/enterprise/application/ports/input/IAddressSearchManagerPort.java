package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;

import java.util.List;

public interface IAddressSearchManagerPort {
    List<Department> getDepartment(Country country);
    List<City> getCities(Department department);

}
