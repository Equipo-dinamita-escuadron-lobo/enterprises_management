package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.mapstruct.Mapper;

@Mapper
public interface IAddressMapper {
    Department tolistModelDeparment(DepartmentEntity deparmentEntities);
    City tolistModelCity(CityEntity citiesEntities);

}
