package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.dto.LocationResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de respuesta que representa la información detallada de una empresa.
 * Se utiliza específicamente para respuestas REST cuando se consulta
 * una empresa por su identificador, incluyendo todos sus datos asociados.
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
public class EnterpriseByIdResponse {
    
    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Nombre o razón social de la empresa.
     */
    private String name;

    /**
     * Número de identificación tributaria.
     */
    private String nit;

    /**
     * Dígito de verificación del NIT.
     */
    private String DV;

    /**
     * Número de teléfono de contacto.
     */
    private String phone;

    /**
     * Sector o rama de actividad de la empresa.
     */
    private String branch;

    /**
     * Correo electrónico de contacto.
     */
    private String email;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;

    /**
     * Estado actual de la empresa.
     */
    private StateEnum state;

    /**
     * Código de la actividad principal.
     */
    private Long mainActivity;

    /**
     * Código de la actividad secundaria.
     */
    private Long secondaryActivity;

    /**
     * Lista de responsabilidades fiscales de la empresa.
     */
    List<TaxLiability> taxLiabilities;

    /**
     * Tipo de contribuyente de la empresa.
     */
    TaxPayerType taxPayerType;

    /**
     * Tipo de empresa.
     */
    EnterpriseType enterpriseType;

    /**
     * Tipo de persona (natural o jurídica).
     */
    PersonType personType;

    /**
     * Ubicación de la empresa.
     */
    LocationResponseDto location;
}
