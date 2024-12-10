package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa la respuesta de una ciudad.
 * Se utiliza para transferir datos de ciudad desde y hacia las interfaces REST.
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
