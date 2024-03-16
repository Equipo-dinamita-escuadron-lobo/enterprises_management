package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper;

import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.CitiesbyDepartmentResponse;
import org.mapstruct.Mapper;

@Mapper
public interface ICitiesbyDepartmentRestMapper {
    CitiesbyDepartmentResponse toResponse(Department department);
}
