package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;

@Mapper
public interface IEnterpriseCreateMapper {

    @Mappings({@Mapping(target = "tenantId", ignore = true)})
    EnterpriseEntity toEntity(Enterprise enterprise);
    Enterprise toModel(EnterpriseEntity enterpriseEntity); 
}
