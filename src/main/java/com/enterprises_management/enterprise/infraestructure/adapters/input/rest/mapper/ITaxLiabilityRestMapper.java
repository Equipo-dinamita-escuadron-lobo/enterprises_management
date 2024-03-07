package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;


@Mapper
public interface ITaxLiabilityRestMapper {
    List<TaxLiabilityResponse> toDomain(List<TaxLiability> taxLiabilities);
    TaxLiabilityResponse toResponse(TaxLiability taxLiability);
}
