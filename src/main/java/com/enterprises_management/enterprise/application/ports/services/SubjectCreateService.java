package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ISubjectCreateManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ISubjectCreateOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de creación de materias.
 * Gestiona la lógica de negocio para el registro de nuevas materias en el sistema,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SubjectCreateService implements ISubjectCreateManagerPort {

    /**
     * Puerto de salida para operaciones de creación de materias.
     */
    private final ISubjectCreateOutputPort subjectCreateOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject createSubject(Subject subject) {
        return subjectCreateOutputPort.create(subject);  
    }
}