package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseSearchOutputPort {
     List<Enterprise> getAllEnterprises();
}
