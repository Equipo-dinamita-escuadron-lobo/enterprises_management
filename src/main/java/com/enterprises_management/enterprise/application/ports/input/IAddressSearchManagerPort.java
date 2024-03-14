package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;


public interface IAddressSearchManagerPort {
    Department getDepartment(Long id);
    City getCity(Long id);

}
