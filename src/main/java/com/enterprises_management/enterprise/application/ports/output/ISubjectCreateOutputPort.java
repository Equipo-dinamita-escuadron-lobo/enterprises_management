package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de salida para la creación de materias.
 * Define las operaciones necesarias para persistir nuevas materias
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectCreateOutputPort {
    
    /**
     * Crea una nueva materia en el sistema de almacenamiento.
     *
     * @param subject Objeto Subject con la información de la materia a crear
     * @return Subject Objeto Subject con la información de la materia creada, incluyendo su identificador
     * @see Subject
     */
    Subject create(Subject subject);
}