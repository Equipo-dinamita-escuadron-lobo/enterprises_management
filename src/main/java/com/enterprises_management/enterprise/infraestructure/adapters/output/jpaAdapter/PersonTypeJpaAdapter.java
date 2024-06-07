package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.IPersonTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.PersonTypeEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.IPersonTypeMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.IPersonTypeRepository;

import lombok.Data;

/**
 * Adaptador para la gestión de tipo de persona usando JPA.
 * Implementa la interfaz IPersonTypeOutputPort.
 */
@Component
@Data
public class PersonTypeJpaAdapter implements IPersonTypeOutputPort{

    private final IPersonTypeRepository personTypeRepository;
    private final IPersonTypeMapper personTypeMapper;

    /**
     * Crea un nuevo tipo de persona.
     *
     * @param personType el modelo de dominio del tipo de persona a crear
     * @return el modelo de dominio del tipo de persona creado, o null si ya existe un tipo de persona con el mismo ID
     */
    @Override
    public PersonType create(PersonType personType) {
        PersonTypeEntity personTypeEntity = personTypeMapper.toEntity(personType);

        // Si la entidad ya tiene un ID, no se puede crear una nueva, por lo que se devuelve null
        if (personTypeEntity.getId() != null) {return null;}
        
        // Guarda la entidad en el repositorio y la convierte de vuelta al modelo de dominio
        return personTypeMapper.toDomain(personTypeRepository.save(personTypeEntity));
    } 
}
