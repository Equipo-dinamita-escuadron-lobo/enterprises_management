package com.enterprises_management.enterprise.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Ciudad.
 * Contiene la información básica de una ciudad para respuestas REST.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityResponseDto {

    /**
     * Identificador único de la ciudad.
     */
    private Long id;

    /**
     * Nombre de la ciudad.
     */
    private String name;
}