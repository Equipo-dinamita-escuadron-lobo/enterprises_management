package com.enterprises_management.enterprise.application.ports.input;

import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseUpdateManagerPort {
    void updateEnterprise(UUID id, Enterprise enterprise);    
    void updateEnterpriseStatus(UUID id, StateEnum state);
}
