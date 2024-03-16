package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseCreateMannegerPort {
    Enterprise createEnterprise(Enterprise enterprise);
}
