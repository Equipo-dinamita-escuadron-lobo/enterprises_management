package com.enterprises_management.enterprise.domain.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.application.ports.output.ITaxLiabilityOutputPort;
import com.enterprises_management.enterprise.domain.models.TaxLiability;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TaxLiabilityService implements ITaxLiabilityManagerPort{

    private final ITaxLiabilityOutputPort taxLiabilityOutputPort;

    @Override
    public List<TaxLiability> getAllTaxLiability() {
        return taxLiabilityOutputPort.getAll();     
    } 
}
