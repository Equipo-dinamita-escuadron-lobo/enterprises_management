package com.enterprises_management.enterprise.application.ports.output;

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
     * @param code el código de la materia a eliminar
     */
    void delete(String code);
}