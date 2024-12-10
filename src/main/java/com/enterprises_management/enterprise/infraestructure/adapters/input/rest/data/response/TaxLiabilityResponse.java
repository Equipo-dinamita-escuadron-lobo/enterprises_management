package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;


import java.util.List;

import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.TaxLiabilityResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de respuesta que representa una lista de responsabilidades fiscales.
 * Se utiliza específicamente para respuestas REST cuando se consultan
 * las responsabilidades fiscales asociadas a una entidad.
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
public class TaxLiabilityResponse {

    /**
     * Identificador único de la responsabilidad fiscal.
     */
    private Long id;

    /**
     * Nombre o descripción de la responsabilidad fiscal.
     */
    private String name;

    /**
     * Lista de responsabilidades fiscales asociadas.
     */
    private List<TaxLiabilityResponseDto> taxLiabilitys;
}
