package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.models.Subject;

/**
 * Puerto de salida para la búsqueda de materias.
 * Define las operaciones necesarias para consultar materias
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectSearchOutputPort {
    
    /**
     * Obtiene todas las materias del sistema de almacenamiento.
     *
     * @return Lista de objetos Subject
     * @see Subject
     */
    List<Subject> findAll();

    /**
     * Obtiene una materia por su código.
     *
     * @param id el código de la materia
     * @return Subject Objeto Subject correspondiente al código, o null si no existe
     * @see Subject
     */
    Subject findByCode(UUID id);
}