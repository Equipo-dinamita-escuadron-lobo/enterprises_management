package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.TaxPayerType;

/**
 * Puerto de entrada para la gestión de tipos de contribuyentes.
 * Define las operaciones disponibles para la consulta y gestión de
 * los diferentes tipos de contribuyentes en el sistema.
 *
 * @author CONTAPP
 * @version 1.0
 * @since 1.0.0
 */
public interface ITaxPayerTypeManagerPort {
    
    /**
     * Recupera todos los tipos de contribuyentes registrados en el sistema.
     *
     * @return List<TaxPayerType> Lista de todos los tipos de contribuyentes disponibles
     * @see TaxPayerType
     */
    List<TaxPayerType> getAllTaxPayerTypes();  
}