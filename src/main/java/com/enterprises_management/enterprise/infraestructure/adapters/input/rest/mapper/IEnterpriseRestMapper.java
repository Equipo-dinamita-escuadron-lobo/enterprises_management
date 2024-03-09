package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseListResponse;

@Mapper
public interface IEnterpriseRestMapper {
    List<EnterpriseListResponse> ToEnterpriseResponseList(List<Enterprise> enterprises);
    EnterpriseListResponse toResponse(Enterprise enterprise);
}
