package com.enterprises_management.enterprise.application.ports.output;

import java.util.List;

import com.enterprises_management.enterprise.domain.models.TaxLiability;

public interface ITaxLiabilityOutputPort {
    List<TaxLiability> getAllTaxLiability();
}
