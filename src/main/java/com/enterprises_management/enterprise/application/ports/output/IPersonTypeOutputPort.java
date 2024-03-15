package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.PersonType;

public interface IPersonTypeOutputPort {
    PersonType create(PersonType personType);
}
