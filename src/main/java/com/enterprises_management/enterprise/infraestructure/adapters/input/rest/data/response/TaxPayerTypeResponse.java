package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.TaxPayerTypeResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de respuesta que representa una lista de tipos de contribuyentes.
 * Se utiliza específicamente para respuestas REST cuando se consultan
 * los diferentes tipos de contribuyentes disponibles en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaxPayerTypeResponse {

    /**
     * Identificador único del tipo de contribuyente.
     */
    private Long id;

    /**
     * Nombre o descripción del tipo de contribuyente.
     */
    private String name;

    /**
     * Lista de tipos de contribuyentes disponibles.
     */
    private List<TaxPayerTypeResponseDto> taxPayerTypes;
}
