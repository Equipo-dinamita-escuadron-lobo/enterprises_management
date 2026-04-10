package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ISubjectDeleteOutputPort;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ISubjectRepository;

import lombok.AllArgsConstructor;
import java.util.UUID;

/**
 * Adaptador para la eliminación de entidades Subject usando JPA.
 * Implementa la interfaz ISubjectDeleteOutputPort.
 */
@Component
@AllArgsConstructor
public class SubjectDeleteJpaAdapter implements ISubjectDeleteOutputPort {

    // Repositorio para realizar operaciones CRUD sobre entidades Subject
    private final ISubjectRepository subjectRepository;

    /**
     * Elimina una entidad Subject por código.
     *
     * @param id el ID de la materia a eliminar
     */
    @Override
    public void delete(UUID id) {
        subjectRepository.findById(id).ifPresent(subjectRepository::delete);
    } 
}