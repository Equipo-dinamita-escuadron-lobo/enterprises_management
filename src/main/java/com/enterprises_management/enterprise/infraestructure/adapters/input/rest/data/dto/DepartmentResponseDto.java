package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa la respuesta de un departamento.
 * Se utiliza para transferir datos de departamento desde y hacia las interfaces REST.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentResponseDto {

    /**
     * Identificador único del departamento.
     */
    private Long id;

    /**
     * Nombre del departamento.
     */
    private String name;
}
