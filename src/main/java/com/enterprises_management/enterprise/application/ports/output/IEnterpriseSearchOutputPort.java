package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.models.Enterprise;

/**
 * Puerto de salida para la búsqueda y consulta de empresas.
 * Define las operaciones necesarias para recuperar información de empresas
 * desde el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseSearchOutputPort {
    
    /**
     * Recupera todas las empresas activas del sistema.
     *
     * @return List<EnterpriseInfoDto> Lista de DTOs con la información básica de las empresas activas
     * @see EnterpriseInfoDto
     */
    List<EnterpriseInfoDto> getAllEnterprises();

    /**
     * Recupera todas las empresas inactivas del sistema.
     *
     * @return List<EnterpriseInfoDto> Lista de DTOs con la información básica de las empresas inactivas
     * @see EnterpriseInfoDto
     */
    List<EnterpriseInfoDto> getAllEnterprisesInactive();

    /**
     * Obtiene una empresa específica por su identificador único.
     *
     * @param id UUID identificador único de la empresa
     * @return Enterprise Objeto con la información completa de la empresa
     * @see Enterprise
     */
    Enterprise getEnterpriseById(UUID id);
}
