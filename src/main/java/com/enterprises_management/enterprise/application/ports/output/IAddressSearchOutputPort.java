package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.Department;


public interface IAddressSearchOutputPort {

    List<Department> getAllDepartment();
    public Department getAllCities(Long idDepartment);
}
