package com.enterprises_management.enterprise.application.ports.output;

import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseUpdateOutputPort {
    void updateEnterprise(UUID id,Enterprise enterprise);
    void updateEnterpriseStatus(UUID id, StateEnum state);
}
