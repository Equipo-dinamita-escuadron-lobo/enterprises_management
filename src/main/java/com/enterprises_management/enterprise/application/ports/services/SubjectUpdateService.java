package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ISubjectUpdateManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ISubjectUpdateOutputPort;
import com.enterprises_management.enterprise.domain.models.Subject;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de actualización de materias.
 * Gestiona la lógica de negocio para modificar materias existentes en el sistema,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SubjectUpdateService implements ISubjectUpdateManagerPort {

    /**
     * Puerto de salida para operaciones de actualización de materias.
     */
    private final ISubjectUpdateOutputPort subjectUpdateOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Subject updateSubject(String code, Subject subject) {
        return subjectUpdateOutputPort.update(code, subject);
    }
}