package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ITaxPayerTypeManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ITaxPayerTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;

import lombok.AllArgsConstructor;

/**
 * Servicio que implementa las operaciones de gestión de tipos de contribuyentes.
 * Gestiona la lógica de negocio para la consulta y recuperación de información
 * sobre los diferentes tipos de contribuyentes en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class TaxPayerTypeService implements ITaxPayerTypeManagerPort{

    /**
     * Puerto de salida para operaciones de gestión de tipos de contribuyentes.
     */
    private final ITaxPayerTypeOutputPort taxPayerTypeOutputPort;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxPayerType> getAllTaxPayerTypes() {
        return taxPayerTypeOutputPort.getAllTaxPayerTypes();     
    } 
}
