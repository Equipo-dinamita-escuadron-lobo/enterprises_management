package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO que representa la información de una materia
 * Se utiliza para transferir datos de materias desde y hacia las interfaces
 * REST,
 * 
 *
 * @author Juan Camilo Zarta Campo
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SubjectDto {
    /**
     * Identificador único de la materia.
     */
    private UUID id;

    /**
     * Código de la materia.
     */
    private String code;

    /**
     * Nombre de la materia.
     */
    private String name;
}
