package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseUpdateOutputPort {
    void updateEnterprise(Long id,Enterprise enterprise);
    void updateEnterpriseStatus(Long id, StateEnum state);
}
