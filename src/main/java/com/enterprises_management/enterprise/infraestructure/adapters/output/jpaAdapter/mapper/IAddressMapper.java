package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;
import org.mapstruct.Mapper;

import java.util.List;
@Mapper
public interface IAddressMapper {
    Department tolistModelDeparment(DepartmentEntity deparmentEntities);
    City tolistModelCity(CityEntity citiesEntities);
    List<Department> toModelListDepartment(List<DepartmentEntity> departmentEntities);
}
