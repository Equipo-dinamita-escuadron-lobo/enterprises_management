package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;


@Mapper
public interface ITaxLiabilityRestMapper {
    List<TaxLiabilityResponse> toDomain(List<TaxLiability> taxLiabilities);
  
    @Mapping( target = "taxLiabilitys", ignore = true)
    TaxLiabilityResponse toResponse(TaxLiability taxLiability);
}
