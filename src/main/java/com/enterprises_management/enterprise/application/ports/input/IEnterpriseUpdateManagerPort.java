package com.enterprises_management.enterprise.application.ports.input;

import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

/**
 * Puerto de entrada para la actualización de empresas.
 * Define las operaciones disponibles para modificar la información y estado
 * de las empresas en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseUpdateManagerPort {
    
    /**
     * Actualiza la información de una empresa existente.
     *
     * @param id UUID identificador único de la empresa a actualizar
     * @param enterprise Objeto Enterprise con la nueva información
     * @see Enterprise
     */
    void updateEnterprise(UUID id, Enterprise enterprise);    

    /**
     * Actualiza el estado de una empresa específica.
     *
     * @param id UUID identificador único de la empresa
     * @param state Nuevo estado a asignar a la empresa
     * @see StateEnum
     */
    void updateEnterpriseStatus(UUID id, StateEnum state);
}
