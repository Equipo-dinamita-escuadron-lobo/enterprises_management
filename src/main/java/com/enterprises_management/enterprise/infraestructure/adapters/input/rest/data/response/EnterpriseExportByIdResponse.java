package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.models.Enterprise;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase de respuesta que representa la información detallada de una empresa.
 * Se utiliza específicamente para respuestas REST cuando se consulta
 * una empresa por su identificador, incluyendo todos sus datos asociados para exportacion.
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

public class EnterpriseExportByIdResponse {
    
    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Identificador del usuario asociado a la empresa.
     */
    private String idUser;

    /**
     * Información completa de la empresa.
     */
    private Enterprise enterprise;

    /**
     * Información de terceros asociados a la empresa.
     */
    private String thirds;
}
