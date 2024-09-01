package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseCreateMannegerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseCreateOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class EnterpriseCreateService  implements IEnterpriseCreateMannegerPort{

    private final IEnterpriseCreateOutputPort enterpriseCreateOutputPort;

    @Override
    public Enterprise createEnterprise(Enterprise enterprise) {
        return enterpriseCreateOutputPort.create(enterprise);  
    }
}
