package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseSearchManagerPort {
    
    List<Enterprise> getAllEnterprises();
}
