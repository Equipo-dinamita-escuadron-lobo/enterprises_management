package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de entrada para la gestión de búsqueda de materias.
 * Define las operaciones necesarias para consultar materias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectSearchManagerPort {
    
    /**
     * Obtiene todas las materias del sistema.
     *
     * @return Lista de objetos Subject
     * @see Subject
     */
    List<Subject> getAllSubjects();

    /**
     * Obtiene una materia por su código.
     *
     * @param id el código de la materia
     * @return Subject Objeto Subject correspondiente al código, o null si no existe
     * @see Subject
     */
    Subject getSubjectByCode(UUID id);
}