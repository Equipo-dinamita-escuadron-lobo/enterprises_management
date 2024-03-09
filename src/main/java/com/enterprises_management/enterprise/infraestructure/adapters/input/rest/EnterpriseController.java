package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseListResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.IEnterpriseRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.ITaxLiabilityRestMapper;

import lombok.AllArgsConstructor;

@RequestMapping("/enterprises")
@RestController
@AllArgsConstructor
public class EnterpriseController {

    private final ITaxLiabilityManagerPort taxLiabilityManagerPort;
    private final ITaxLiabilityRestMapper taxLiabilityRestMapper;

    private final IEnterpriseSearchManagerPort enterpriseSearchManagerPort;
    private final IEnterpriseRestMapper enterpriseRestMapper;


    @GetMapping("/taxliabilities")
    public ResponseEntity<List<TaxLiabilityResponse>> getAllTaxLiability(){
        List<TaxLiability> taxLiability = taxLiabilityManagerPort.getAllTaxLiability();
        return ResponseEntity.ok(taxLiabilityRestMapper.toDomain(taxLiability));
    }

    @GetMapping("/")
    public ResponseEntity<List<EnterpriseListResponse>> getAllEnterprises(){
        List<Enterprise> enterprises = enterpriseSearchManagerPort.getAllEnterprises();
        return ResponseEntity.ok(enterpriseRestMapper.ToEnterpriseResponseList(enterprises));
    }
    
}
