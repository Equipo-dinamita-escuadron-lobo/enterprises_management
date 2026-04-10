package com.enterprises_management.enterprise.application.ports.input;
import java.util.UUID;

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
     * @param id el código de la materia a eliminar
     */
    void deleteSubject(UUID id);
}