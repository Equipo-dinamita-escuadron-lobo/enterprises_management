package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;


import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;

public interface IEnterpriseCreateRestMapper {

    Enterprise toDomain(EnterpriseCreateRequest enterpriseCreateResponse);

    EnterpriseCreateResponse toCreateResponse(Enterprise enterprise);
}
