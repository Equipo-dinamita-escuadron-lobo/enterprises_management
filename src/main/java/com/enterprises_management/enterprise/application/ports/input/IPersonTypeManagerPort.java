package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.PersonType;

public interface IPersonTypeManagerPort {
    PersonType createPersonType(PersonType personType);
}
