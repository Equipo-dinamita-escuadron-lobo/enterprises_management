package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Country;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.TaxLiability;

import java.util.List;

public interface IAddressSearchManagerPort {
    Department getDepartment(Long id);
    City getCity(Long id);
    List<Department> getAllDepartment(); 
}
