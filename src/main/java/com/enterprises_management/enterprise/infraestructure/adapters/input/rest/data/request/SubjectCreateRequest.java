package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para crear una nueva materia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectCreateRequest {

    /**
     * Código de la materia.
     */
    @NotBlank(message = "El código de la materia es obligatorio")
    private String code;

    /**
     * Nombre de la materia.
     */
    @NotBlank(message = "El nombre de la materia es obligatorio")
    private String name;
}