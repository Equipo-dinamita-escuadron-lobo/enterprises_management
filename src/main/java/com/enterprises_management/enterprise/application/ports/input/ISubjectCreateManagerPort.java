package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de entrada para la gestión de creación de materias.
 * Define las operaciones necesarias para el registro y creación de nuevas materias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectCreateManagerPort {
    
    /**
     * Crea una nueva materia en el sistema.
     *
     * @param subject Objeto Subject con la información de la materia a crear
     * @return Subject Objeto Subject con la información de la materia creada, incluyendo su ID
     * @see Subject
     */
    Subject createSubject(Subject subject);
}