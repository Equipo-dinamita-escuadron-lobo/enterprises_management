package com.enterprises_management.enterprise.application.ports.input;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.TaxLiability;

public interface ITaxLiabilityManagerPort {
    List<TaxLiability> getAllTaxLiability();  
    
}
