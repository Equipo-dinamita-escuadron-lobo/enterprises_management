package com.enterprises_management.enterprise.application.ports.services;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseCreateMannegerPort;
import com.enterprises_management.enterprise.application.ports.output.IEnterpriseCreateOutputPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de creación de empresas.
 * Gestiona la lógica de negocio para el registro de nuevas empresas en el sistema,
 * actuando como intermediario entre los puertos de entrada y salida.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class EnterpriseCreateService implements IEnterpriseCreateMannegerPort {

    /**
     * Puerto de salida para operaciones de creación de empresas.
     */
    private final IEnterpriseCreateOutputPort enterpriseCreateOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public Enterprise createEnterprise(Enterprise enterprise) {
        return enterpriseCreateOutputPort.create(enterprise);  
    }
}
