package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.Department;


public interface IAddressSearchManagerPort {

    List<Department> getAllDepartment();
    public Department getAllCities(Long idDepartment);
}
