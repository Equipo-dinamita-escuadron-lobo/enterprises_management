package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import com.enterprises_management.enterprise.domain.models.City;
import com.enterprises_management.enterprise.domain.models.Department;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.CityEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.DepartmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Mapper
public interface ICitiesbyDepartmentMapper {

    @Mapping(target = "cities", qualifiedByName = "mapCities")
    DepartmentEntity toEntity(Department department);

    @Mapping(target = "cities", qualifiedByName = "mapCitiesDomain")
    Department toDomain(DepartmentEntity departmentEntity);

    @Named("mapCities")
    default List<CityEntity> mapCities(List<City> cities) {
        if (cities == null) {
            return null;
        }
        return cities.stream()
                .map(city -> {
                    CityEntity cityEntity = new CityEntity();
                    cityEntity.setId(city.getId());
                    cityEntity.setName(city.getName());
                    return cityEntity;
                })
                .collect(Collectors.toList());
    }

    @Named("mapCitiesDomain")
    default List<City> mapCitiesDomain(List<CityEntity> cities) {
        if (cities == null) {
            return null;
        }
        return cities.stream()
                .map(cityEntity -> {
                    City city = new City();
                    city.setId(cityEntity.getId());
                    city.setName(cityEntity.getName());
                    return city;
                })
                .collect(Collectors.toList());
    }
}
