package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;

@Mapper
public interface IEnterpriseCreateMapper {

    EnterpriseEntity toEntity(Enterprise enterprise);
    Enterprise toModel(EnterpriseEntity enterpriseEntity); 
}
