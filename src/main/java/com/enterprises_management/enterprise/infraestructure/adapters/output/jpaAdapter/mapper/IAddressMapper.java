package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;

import java.util.List;

import org.mapstruct.Mapper;

@Mapper
public interface IAddressMapper {
    Department tolistModelDeparment(DepartmentEntity deparmentEntities);
    City tolistModelCity(CityEntity citiesEntities);
    List<Department> toModelListDepartment(List<DepartmentEntity> departmentEntities);
}
