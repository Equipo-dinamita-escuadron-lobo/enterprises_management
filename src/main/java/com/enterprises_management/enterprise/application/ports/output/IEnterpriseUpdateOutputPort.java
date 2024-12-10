package com.enterprises_management.enterprise.application.ports.output;

import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

/**
 * Puerto de salida para la actualización de empresas.
 * Define las operaciones necesarias para modificar la información de empresas
 * en el sistema de almacenamiento.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface IEnterpriseUpdateOutputPort {
    
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
