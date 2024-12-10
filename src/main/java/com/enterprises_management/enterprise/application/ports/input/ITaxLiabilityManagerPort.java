package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.TaxLiability;

/**
 * Puerto de entrada para la gestión de responsabilidades fiscales.
 * Define las operaciones disponibles para la consulta y gestión de
 * obligaciones tributarias en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ITaxLiabilityManagerPort {
    
    /**
     * Recupera todas las responsabilidades fiscales registradas en el sistema.
     *
     * @return List<TaxLiability> Lista de todas las responsabilidades fiscales disponibles
     * @see TaxLiability
     */
    List<TaxLiability> getAllTaxLiability();  
}
