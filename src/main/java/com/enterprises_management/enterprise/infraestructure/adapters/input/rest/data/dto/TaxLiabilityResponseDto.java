package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa la respuesta de una responsabilidad fiscal.
 * Se utiliza para transferir datos de responsabilidades fiscales desde y hacia las interfaces REST.
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
public class TaxLiabilityResponseDto {

    /**
     * Identificador único de la responsabilidad fiscal.
     */
    private Long id;

    /**
     * Nombre o descripción de la responsabilidad fiscal.
     */
    private String name;
}
