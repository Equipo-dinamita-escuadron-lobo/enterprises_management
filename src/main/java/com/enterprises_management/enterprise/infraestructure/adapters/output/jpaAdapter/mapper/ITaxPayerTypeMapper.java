package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.TaxPayerTypeEntity;

@Mapper
public interface ITaxPayerTypeMapper {
     
    TaxPayerTypeEntity toEntity(TaxPayerType taxPayerType);
    List<TaxPayerType> toModel(List<TaxPayerTypeEntity> taxPayerTypeEntities);
}