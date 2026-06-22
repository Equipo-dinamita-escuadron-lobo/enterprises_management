package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ISubjectCreateOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ISubjectCreateMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.AllArgsConstructor;

/**
 * Adaptador para la creación de entidades Subject usando JPA.
 * Implementa la interfaz ISubjectCreateOutputPort.
 */
@Component
@AllArgsConstructor
public class SubjectCreateJpaAdapter implements ISubjectCreateOutputPort {

    // Repositorio para realizar operaciones CRUD sobre entidades Subject
    private final ISubjectRepository subjectRepository;
    
    // Mapper para convertir entre modelos de dominio y entidades JPA
    private final ISubjectCreateMapper createMapper;

    /**
     * Crea una nueva entidad Subject.
     *
     * @param subject el modelo de dominio de la Materia a crear
     * @return el modelo de dominio de la Materia creada, o null si la conversión a entidad falla
     */
    @Override
    public Subject create(Subject subject) {
        // Convierte el modelo de dominio a una entidad JPA
        SubjectEntity subjectEntity = createMapper.toEntity(subject);
        
        if (subjectEntity == null) {
            return null;          
        }
        
        // Guarda la entidad en el repositorio (base de datos)
        subjectEntity = subjectRepository.save(subjectEntity);
        
        // Convierte la entidad guardada de vuelta al modelo de dominio y la devuelve
        return createMapper.toModel(subjectEntity);
    } 
}