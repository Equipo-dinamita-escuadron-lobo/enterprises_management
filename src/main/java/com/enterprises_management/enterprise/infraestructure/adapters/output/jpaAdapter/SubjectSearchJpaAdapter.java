package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ISubjectSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.entity.SubjectEntity;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ISubjectSearchMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.AllArgsConstructor;

/**
 * Adaptador para la búsqueda de entidades Subject usando JPA.
 * Implementa la interfaz ISubjectSearchOutputPort.
 */
@Component
@AllArgsConstructor
public class SubjectSearchJpaAdapter implements ISubjectSearchOutputPort {

    // Repositorio para realizar operaciones de consulta sobre entidades Subject
    private final ISubjectRepository subjectRepository;
    
    // Mapper para convertir entre modelos de dominio y entidades JPA
    private final ISubjectSearchMapper searchMapper;

    /**
     * Obtiene todas las entidades Subject.
     *
     * @return Lista de modelos de dominio Subject
     */
    @Override
    public List<Subject> findAll() {
        List<SubjectEntity> entities = subjectRepository.findAll();
        return entities.stream()
                .map(searchMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una entidad Subject por código.
     *
     * @param code el código de la materia
     * @return el modelo de dominio Subject correspondiente, o null si no existe
     */
    @Override
    public Subject findByCode(String code) {
        Optional<SubjectEntity> entity = subjectRepository.findByCode(code);
        return entity.map(searchMapper::toModel).orElse(null);
    }
}