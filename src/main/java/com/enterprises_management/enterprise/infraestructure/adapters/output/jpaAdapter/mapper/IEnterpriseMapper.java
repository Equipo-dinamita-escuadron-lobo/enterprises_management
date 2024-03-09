package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseEntity;

@Mapper
public interface IEnterpriseMapper {

    EnterpriseEntity toEntity(Enterprise enterprise);
    List<Enterprise> toListModel(List<EnterpriseEntity> enterpriseEntities);  
}
