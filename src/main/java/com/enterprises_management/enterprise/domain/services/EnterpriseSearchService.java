package com.enterprises_management.enterprise.domain.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class EnterpriseSearchService implements IEnterpriseSearchManagerPort {

    private final IEnterpriseSearchOutputPort enterpriseSearchOutputPort;

    @Override
    public List<Enterprise> getAllEnterprises() {
        return enterpriseSearchOutputPort.getAllEnterprises();
    } 
}
