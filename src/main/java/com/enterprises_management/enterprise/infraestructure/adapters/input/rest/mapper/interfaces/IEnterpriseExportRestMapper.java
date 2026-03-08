package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces;
import com.enterprises_management.enterprise.domain.models.EnterpriseExport;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseExportByIdResponse;

public interface IEnterpriseExportRestMapper {
    
    EnterpriseExportByIdResponse toResponse(EnterpriseExport enterpriseExport);
}

