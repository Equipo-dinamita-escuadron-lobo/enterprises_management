package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxPayerTypeResponse;


@Mapper
public interface ITaxPayerTypeRestMapper {
    List<TaxPayerTypeResponse> toDomain(List<TaxPayerType> taxPayerType);
    TaxPayerTypeResponse toResponse(TaxPayerType taxPayerType);
}
