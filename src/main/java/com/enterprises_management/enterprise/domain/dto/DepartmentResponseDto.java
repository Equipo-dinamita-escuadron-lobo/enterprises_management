package com.enterprises_management.enterprise.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Departamento.
 * Contiene la información básica de un departamento para respuestas REST.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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