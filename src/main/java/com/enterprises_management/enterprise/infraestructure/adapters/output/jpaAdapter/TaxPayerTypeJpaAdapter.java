package com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.enterprises_management.enterprise.application.ports.output.ITaxPayerTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.mapper.ITaxPayerTypeMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.output.jpaAdapter.repository.ITaxPayerTypeRepository;

import lombok.Data;

/**
 * Adaptador para la gestión de tipo de contribuyentes usando JPA.
 * Implementa la interfaz ITaxPayerTypeOutputPort.
 */
@Component
@Data
public class TaxPayerTypeJpaAdapter implements ITaxPayerTypeOutputPort{

    private final ITaxPayerTypeRepository taxPayerTypeRepository;
    private final ITaxPayerTypeMapper taxPayerTypeMapper;

    /**
     * Obtiene todas los tipos de contribuyente.
     *
     * @return una lista de todas los tipos de contribuyente en el modelo de dominio
     */

    @Override
    public List<TaxPayerType> getAllTaxPayerTypes() {
        return taxPayerTypeMapper.toModel(taxPayerTypeRepository.findAll());
    }  
}