package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ISubjectUpdateOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ISubjectUpdateMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.AllArgsConstructor;

/**
 * Adaptador para la actualización de entidades Subject usando JPA.
 * Implementa la interfaz ISubjectUpdateOutputPort.
 */
@Component
@AllArgsConstructor
public class SubjectUpdateJpaAdapter implements ISubjectUpdateOutputPort {

    // Repositorio para realizar operaciones CRUD sobre entidades Subject
    private final ISubjectRepository subjectRepository;
    
    // Mapper para convertir entre modelos de dominio y entidades JPA
    private final ISubjectUpdateMapper updateMapper;

    /**
     * Actualiza una entidad Subject existente.
     *
     * @param code el código de la materia a actualizar
     * @param subject el modelo de dominio con la nueva información
     * @return el modelo de dominio de la Materia actualizada, o null si no existe
     */
    @Override
    public Subject update(String code, Subject subject) {
        // Busca la entidad existente por código
        SubjectEntity existingEntity = subjectRepository.findByCode(code).orElse(null);
        if (existingEntity == null) {
            return null;
        }
        
        // Actualiza los campos
        existingEntity.setCode(subject.getCode());
        existingEntity.setName(subject.getName());
        
        // Guarda la entidad actualizada
        existingEntity = subjectRepository.save(existingEntity);
        
        // Convierte de vuelta al modelo de dominio
        return updateMapper.toModel(existingEntity);
    } 
}