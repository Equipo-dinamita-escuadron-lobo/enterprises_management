package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;


public interface IEnterpriseSearchManagerPort {
    
    List<EnterpriseInfoDto> getAllEnterprises();
    List<EnterpriseInfoDto> getAllEnterprisesInactive();
}
