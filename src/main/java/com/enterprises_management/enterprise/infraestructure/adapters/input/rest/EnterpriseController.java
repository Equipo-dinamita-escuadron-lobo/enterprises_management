package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.ITaxLiabilityRestMapper;

@RequestMapping("/enterprises")
@RestController
public class EnterpriseController {

    private final ITaxLiabilityManagerPort taxLiabilityManagerPort;
    private final ITaxLiabilityRestMapper taxLiabilityRestMapper;

    public EnterpriseController(ITaxLiabilityManagerPort taxLiabilityManagerPort, ITaxLiabilityRestMapper taxLiabilityRestMapper) {
        this.taxLiabilityManagerPort = taxLiabilityManagerPort;
        this.taxLiabilityRestMapper = taxLiabilityRestMapper;
    }

    @GetMapping("/taxliabilities")
    public ResponseEntity<List<TaxLiabilityResponse>> getAllTaxLiability(){
        List<TaxLiability> taxLiability = taxLiabilityManagerPort.getAllTaxLiability();
        return ResponseEntity.ok(taxLiabilityRestMapper.toDomain(taxLiability));
    }
    
}
