package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.LocationEntity;

@Mapper
public interface ILocationMapper {
    Location toDomain(LocationEntity locationEntity);
    LocationEntity toEntity(Location location); 
}
