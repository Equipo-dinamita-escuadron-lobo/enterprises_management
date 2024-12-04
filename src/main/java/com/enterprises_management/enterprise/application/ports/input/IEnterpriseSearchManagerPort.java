package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.EnterpriseTypeEntity;


public interface IEnterpriseSearchManagerPort {
    
    List<EnterpriseInfoDto> getAllEnterprises();
    List<EnterpriseInfoDto> getAllEnterprisesInactive();
    Enterprise getEnterpriseById(UUID id);
    List<EnterpriseTypeEntity> getTypeEnterprises();
}
