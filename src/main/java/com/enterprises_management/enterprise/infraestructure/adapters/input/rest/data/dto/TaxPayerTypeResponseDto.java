package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que representa la respuesta de un tipo de contribuyente.
 * Se utiliza para transferir datos de tipos de contribuyentes desde y hacia las interfaces REST.
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
public class TaxPayerTypeResponseDto {

    /**
     * Identificador único del tipo de contribuyente.
     */
    private Long id;

    /**
     * Nombre o descripción del tipo de contribuyente.
     */
    private String name;
}