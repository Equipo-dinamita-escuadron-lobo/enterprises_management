package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;

@Mapper
public interface IPersonTypeMapper {
    PersonTypeEntity toEntity(PersonType personType);
    PersonType toDomain(PersonTypeEntity personTypeEntity); 
}
