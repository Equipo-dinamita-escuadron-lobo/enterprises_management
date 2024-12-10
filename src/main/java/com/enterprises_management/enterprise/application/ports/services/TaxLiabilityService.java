package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxLiability;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de gestión de responsabilidades fiscales.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * sobre las responsabilidades tributarias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class TaxLiabilityService implements ITaxLiabilityManagerPort {

    /**
     * Puerto de salida para operaciones de gestión de responsabilidades fiscales.
     */
    private final ITaxLiabilityOutputPort taxLiabilityOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxLiability> getAllTaxLiability() {
        return taxLiabilityOutputPort.getAllTaxLiability();     
    } 
}
