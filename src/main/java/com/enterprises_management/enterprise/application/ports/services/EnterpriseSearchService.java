package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.TypeEnterpriseResponseDto;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class EnterpriseSearchService implements IEnterpriseSearchManagerPort {

    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;

    @Override
    public List<EnterpriseInfoDto> getAllEnterprises() {
        return enterpriseSearchOutputPort.getAllEnterprises();
    }

    @Override
    public List<EnterpriseInfoDto> getAllEnterprisesInactive() {
        return enterpriseSearchOutputPort.getAllEnterprisesInactive();
    }

    @Override
    public Enterprise getEnterpriseById(UUID id) {
        return enterpriseSearchOutputPort.getEnterpriseById(id);
    }

    @Override
    public List<EnterpriseTypeEntity> getTypeEnterprises() {
        return enterpriseSearchOutputPort.getAllTypeEnterprises();
    }
}
