package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ISubjectSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ISubjectSearchOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;

import lombok.AllArgsConstructor;
import java.util.UUID;


/**
 * Servicio que implementa las operaciones de búsqueda de materias.
 * Gestiona la lógica de negocio para consultar materias en el sistema,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SubjectSearchService implements ISubjectSearchManagerPort {

    /**
     * Puerto de salida para operaciones de búsqueda de materias.
     */
    private final ISubjectSearchOutputPort subjectSearchOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subject> getAllSubjects() {
        return subjectSearchOutputPort.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject getSubjectByCode(UUID id) {
        return subjectSearchOutputPort.findByCode(id);
    }
}