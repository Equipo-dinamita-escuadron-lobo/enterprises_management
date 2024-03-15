package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IPersonTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IPersonTypeMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IPersonTypeRepository;

import lombok.Data;

@Component
@Data
public class PersonTypeJpaAdapter implements IPersonTypeOutputPort{

    private final IPersonTypeRepository personTypeRepository;
    private final IPersonTypeMapper personTypeMapper;

    @Override
    public PersonType create(PersonType personType) {
        PersonTypeEntity personTypeEntity = personTypeMapper.toEntity(personType);
        if (personTypeEntity.getId() != null) {return null;}

        return personTypeMapper.toDomain(personTypeRepository.save(personTypeEntity));
    } 
}
