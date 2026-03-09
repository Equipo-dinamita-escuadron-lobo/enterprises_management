package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.impl;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.domain.models.EnterpriseExport;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseExportByIdResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseExportRestMapper;

@Component
public class EnterpriseExportRestMapper implements IEnterpriseExportRestMapper {
    @Override
    public EnterpriseExportByIdResponse toResponse(EnterpriseExport export) {
        if (export == null) return null;
        return EnterpriseExportByIdResponse.builder()
                .id(export.getId())
                .idUser(export.getIdUser())
                .enterprise(export.getEnterprise())
                .thirds(export.getThirds())
                .build();
    }
}
