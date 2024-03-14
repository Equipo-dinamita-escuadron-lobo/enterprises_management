package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import org.mapstruct.Mapper;
import java.util.List;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.CityAddressResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.DepartmentAddressResponse;
@Mapper
public interface IAddressRestMapper {
    CityAddressResponse ToCityResponseList(City cities);
    DepartmentAddressResponse toDepartmentResponseList(Department department);
    List<DepartmentAddressResponse> toDepartmentListResponse(List<Department> departments);
}
