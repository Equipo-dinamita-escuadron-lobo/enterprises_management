package com.enterprises_management.enterprise.domain.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IPersonTypeManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IPersonTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.PersonType;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PersonTypeService implements IPersonTypeManagerPort {

    private final IPersonTypeOutputPort personTypeOutputPort;

    @Override
    public PersonType createPersonType(PersonType personType) {
        return personTypeOutputPort.create(personType);
    }
    
}
