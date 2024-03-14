package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseCreateOutputPort {
        Enterprise create(Enterprise enterprise);
}
