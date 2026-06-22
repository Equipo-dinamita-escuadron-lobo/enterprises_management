package com.enterprises_management.enterprise.application.ports.output;

import java.util.UUID;

/**
 * Puerto de salida para la eliminación de materias.
 * Define las operaciones necesarias para eliminar materias
 * del sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectDeleteOutputPort {
    
    /**
     * Elimina una materia por su código.
     *
     * @param id el código de la materia a eliminar
     */
    void delete(UUID id);
}