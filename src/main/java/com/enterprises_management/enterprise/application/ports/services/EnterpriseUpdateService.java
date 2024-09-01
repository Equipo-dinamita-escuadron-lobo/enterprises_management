package com.enterprises_management.enterprise.application.ports.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

@Service
public class EnterpriseUpdateService implements IEnterpriseUpdateManagerPort {

    @Autowired
    private IEnterpriseUpdateOutputPort enterpriseUpdateOutputPort;

    @Override
    public void updateEnterprise(UUID id, Enterprise enterprise) {
        enterpriseUpdateOutputPort.updateEnterprise(id, enterprise);   
    }

    @Override
    public void updateEnterpriseStatus(UUID id, StateEnum state) {  
        enterpriseUpdateOutputPort.updateEnterpriseStatus(id, state);
    }
    
}
