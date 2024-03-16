package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface IDepartmentRestMapper {


    List<DepartmentAddressResponse> toDepartmentResponseList(List<Department> department);
}
