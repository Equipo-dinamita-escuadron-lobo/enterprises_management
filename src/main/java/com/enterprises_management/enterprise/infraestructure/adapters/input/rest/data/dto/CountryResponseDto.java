package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa la respuesta de un país.
 * Se utiliza para transferir datos de país desde y hacia las interfaces REST.
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
public class CountryResponseDto {

    /**
     * Identificador único del país.
     */
    private Long id;

    /**
     * Nombre del país.
     */
    private String name;
}
