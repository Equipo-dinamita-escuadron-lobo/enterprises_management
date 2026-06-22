package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxPayerTypeResponse;


@Mapper
public interface ITaxPayerTypeRestMapper {
    List<TaxPayerTypeResponse> toDomain(List<TaxPayerType> taxPayerType);
    @Mapping(target = "taxPayerTypes", ignore = true)
    TaxPayerTypeResponse toResponse(TaxPayerType taxPayerType);
}
