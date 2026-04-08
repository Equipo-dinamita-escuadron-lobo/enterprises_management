package com.enterprises_management.enterprise.application.ports.input;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de entrada para la gestión de actualización de materias.
 * Define las operaciones necesarias para modificar materias existentes en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectUpdateManagerPort {
    
    /**
     * Actualiza una materia existente en el sistema.
     *
     * @param code el código de la materia a actualizar
     * @param subject Objeto Subject con la nueva información
     * @return Subject Objeto Subject actualizado
     * @see Subject
     */
    Subject updateSubject(String code, Subject subject);
}