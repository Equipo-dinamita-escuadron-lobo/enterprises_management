package com.enterprises_management.enterprise.application.ports.input;

/**
 * Puerto de entrada para la gestión de eliminación de materias.
 * Define las operaciones necesarias para eliminar materias del sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ISubjectDeleteManagerPort {
    
    /**
     * Elimina una materia por su código.
     *
     * @param code el código de la materia a eliminar
     */
    void deleteSubject(String code);
}