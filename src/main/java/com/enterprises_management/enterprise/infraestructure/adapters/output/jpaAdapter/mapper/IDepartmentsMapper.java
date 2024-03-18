package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CountryEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface IDepartmentsMapper {


    @Mapping(target = "country", ignore = true)
    @Mapping(target = "cities", ignore = true)
    DepartmentEntity toEntity(Department department);

    @Mapping(target = "department.country", ignore = true)
    @Mapping(target = "cities", ignore = true)
    Department toModel(DepartmentEntity departmentEntity);

    default List<DepartmentEntity> toEntityList(List<Department> departmentList) {
        if (departmentList == null) {
            return null;
        }

        return departmentList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    default List<Department> toModelList(List<DepartmentEntity> departmentEntityList) {
        if (departmentEntityList == null) {
            return null;
        }

        return departmentEntityList.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
