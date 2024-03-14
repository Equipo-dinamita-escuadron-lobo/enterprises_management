package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;

import java.util.List;

import org.mapstruct.Mapper;

import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseListResponse;

@Mapper
public interface IEnterpriseRestMapper {
    //Se crea un mapeo de la lista de empresas a la lista de respuestas de empresas
    List<EnterpriseListResponse> ToEnterpriseResponseList(List<Enterprise> enterprises);
    EnterpriseListResponse toResponse(Enterprise enterprise);

}
