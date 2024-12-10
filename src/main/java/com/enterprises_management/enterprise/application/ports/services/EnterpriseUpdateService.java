package com.enterprises_management.enterprise.application.ports.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseUpdateOutputPort;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;

/**
 * Servicio que implementa las operaciones de actualización de empresas.
 * Gestiona la lógica de negocio para la modificación de información y estado
 * de las empresas en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
public class EnterpriseUpdateService implements IEnterpriseUpdateManagerPort {

    /**
     * Puerto de salida para operaciones de actualización de empresas.
     */
    @Autowired
    private IEnterpriseUpdateOutputPort enterpriseUpdateOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEnterprise(UUID id, Enterprise enterprise) {
        enterpriseUpdateOutputPort.updateEnterprise(id, enterprise);   
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateEnterpriseStatus(UUID id, StateEnum state) {  
        enterpriseUpdateOutputPort.updateEnterpriseStatus(id, state);
    }
    
}
