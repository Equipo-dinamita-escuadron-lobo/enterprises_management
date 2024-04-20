package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;

public interface IEnterpriseSearchOutputPort {
     List<EnterpriseInfoDto> getAllEnterprises();
     List<EnterpriseInfoDto> getAllEnterprisesInactive();
}
