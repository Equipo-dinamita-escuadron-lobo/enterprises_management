package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de salida para la actualización de materias.
 * Define las operaciones necesarias para modificar materias existentes
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectUpdateOutputPort {
    
    /**
     * Actualiza una materia existente en el sistema de almacenamiento.
     *
     * @param code el código de la materia a actualizar
     * @param subject Objeto Subject con la nueva información
     * @return Subject Objeto Subject actualizado
     * @see Subject
     */
    Subject update(String code, Subject subject);
}