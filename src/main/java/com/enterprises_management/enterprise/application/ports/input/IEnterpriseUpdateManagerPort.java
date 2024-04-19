package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseUpdateManagerPort {
    void updateEnterprise(Long id, Enterprise enterprise);     
}
