package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta para consultas de materia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectResponse {

    /**
     * Código de la materia.
     */
    private String code;

    /**
     * Nombre de la materia.
     */
    private String name;
}