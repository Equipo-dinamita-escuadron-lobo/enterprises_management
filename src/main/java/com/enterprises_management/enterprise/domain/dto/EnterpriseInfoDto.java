package com.enterprises_management.enterprise.domain.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) que representa la información básica de una empresa.
 * Se utiliza para transferir datos simplificados de empresas entre diferentes
 * capas de la aplicación.
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
public class EnterpriseInfoDto {
    
    /**
     * Identificador único de la empresa.
     */
    private UUID id;

    /**
     * Nombre o razón social de la empresa.
     */
    private String name;

    /**
     * Número de identificación tributaria de la empresa.
     */
    private String nit;

    /**
     * URL o ruta del logo de la empresa.
     */
    private String logo;
}
