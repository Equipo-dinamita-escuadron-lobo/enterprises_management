package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseSearchOutputPort {
     List<EnterpriseInfoDto> getAllEnterprises();
     List<EnterpriseInfoDto> getAllEnterprisesInactive();
     Enterprise getEnterpriseById(UUID id);
}
