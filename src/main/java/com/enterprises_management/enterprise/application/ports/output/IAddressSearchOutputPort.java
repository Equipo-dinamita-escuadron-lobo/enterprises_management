package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;


public interface IAddressSearchOutputPort {
    Department getDepartment(Long id);
    City getCity(Long id);
    List<Department> getAllDepartment();
}
