package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxLiabilityEntity;

@Mapper
public interface ITaxLiabilityMapper {
     
    TaxLiabilityEntity toEntity(TaxLiability taxLiability);
    List<TaxLiability> toModel(List<TaxLiabilityEntity> taxLiabilityEntities);
}
