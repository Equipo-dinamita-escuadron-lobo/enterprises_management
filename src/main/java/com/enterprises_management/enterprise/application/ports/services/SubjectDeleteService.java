package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ISubjectDeleteManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ISubjectDeleteOutputPort;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de eliminación de materias.
 * Gestiona la lógica de negocio para eliminar materias del sistema,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SubjectDeleteService implements ISubjectDeleteManagerPort {

    /**
     * Puerto de salida para operaciones de eliminación de materias.
     */
    private final ISubjectDeleteOutputPort subjectDeleteOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteSubject(String code) {
        subjectDeleteOutputPort.delete(code);
    }
}