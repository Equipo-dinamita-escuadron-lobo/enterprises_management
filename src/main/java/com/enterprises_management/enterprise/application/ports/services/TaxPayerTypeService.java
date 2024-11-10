package com.enterprises_management.enterprise.application.ports.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ITaxPayerTypeManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ITaxPayerTypeOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaxPayerTypeService implements ITaxPayerTypeManagerPort{

    private final ITaxPayerTypeOutputPort taxPayerTypeOutputPort;

    @Override
    public List<TaxPayerType> getAllTaxPayerTypes() {
        return taxPayerTypeOutputPort.getAllTaxPayerTypes();     
    } 
}
