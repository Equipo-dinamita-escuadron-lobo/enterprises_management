package com.enterprises_management.enterprise.infraestructure.adapters.input.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.enterprises_management.enterprise.application.ports.input.IEnterpriseCreateMannegerPort;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseSearchManagerPort;
import com.enterprises_management.enterprise.application.ports.input.IEnterpriseUpdateManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ILocationMangerPort;
import com.enterprises_management.enterprise.application.ports.input.IPersonTypeManagerPort;
import com.enterprises_management.enterprise.application.ports.input.ITaxLiabilityManagerPort;
import com.enterprises_management.enterprise.domain.dto.EnterpriseInfoDto;
import com.enterprises_management.enterprise.domain.enums.StateEnum;
import com.enterprises_management.enterprise.domain.models.Enterprise;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.request.EnterpriseCreateRequest;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.EnterpriseCreateResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response.TaxLiabilityResponse;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.IEnterpriseCreateRestMapper;
import com.enterprises_management.enterprise.infraestructure.adapters.input.rest.mapper.interfaces.ITaxLiabilityRestMapper;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RequestMapping("/api/enterprises")
@RestController
@AllArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class EnterpriseController {

    private final ITaxLiabilityManagerPort taxLiabilityManagerPort;
    private final ITaxLiabilityRestMapper taxLiabilityRestMapper;

    private final IEnterpriseSearchManagerPort enterpriseSearchManagerPort;

    private final IEnterpriseCreateMannegerPort enterpriseCreateMannegerPort;
    private final IEnterpriseCreateRestMapper enterpriseCreateMapper;

    private final ILocationMangerPort locationMangerPort;
    private final IPersonTypeManagerPort personTypeManagerPort;

    private final IEnterpriseUpdateManagerPort enterpriseUpdateManagerPort;

    @GetMapping("/taxliabilities")
    public ResponseEntity<List<TaxLiabilityResponse>> getAllTaxLiability(){
        List<TaxLiability> taxLiability = taxLiabilityManagerPort.getAllTaxLiability();
        return ResponseEntity.ok(taxLiabilityRestMapper.toDomain(taxLiability));
    }

    @GetMapping("/")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprises(){
        List<EnterpriseInfoDto> enterprises = enterpriseSearchManagerPort.getAllEnterprises();
        return ResponseEntity.ok(enterprises);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<EnterpriseInfoDto>> getAllEnterprisesInactive(){
        List<EnterpriseInfoDto> enterprises = enterpriseSearchManagerPort.getAllEnterprisesInactive();
        return ResponseEntity.ok(enterprises);
    }

    @PostMapping("/")
    public ResponseEntity<EnterpriseCreateResponse> createEnterprise(@Valid @RequestBody EnterpriseCreateRequest enterpriseCreateRequest){ 
            Enterprise enterprise = enterpriseCreateMapper.toDomain(enterpriseCreateRequest);

            enterprise.setLocation(locationMangerPort.createLocation(enterprise.getLocation()));
            enterprise.setPersonType(personTypeManagerPort.createPersonType(enterprise.getPersonType()));

            enterprise = enterpriseCreateMannegerPort.createEnterprise(enterprise);
            return ResponseEntity.ok(enterpriseCreateMapper.toCreateResponse(enterprise));   
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEnterprise(@PathVariable("id") Long id, @Valid @RequestBody EnterpriseCreateRequest enterpriseCreateRequest){
        Enterprise enterprise = enterpriseCreateMapper.toDomain(enterpriseCreateRequest);

        enterprise.setLocation(locationMangerPort.createLocation(enterprise.getLocation()));
        enterprise.setPersonType(personTypeManagerPort.createPersonType(enterprise.getPersonType()));
        
        enterpriseUpdateManagerPort.updateEnterprise(id, enterprise);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/status/{id}/{state}")
    public ResponseEntity<?> updateEnterpriseStatus(@PathVariable("id") Long id, @PathVariable("state") String state){
        try {
            StateEnum stateEnum ;

            switch (state) {
                case "ACTIVE":
                    stateEnum = StateEnum.ACTIVE;
                    break;
                case "INACTIVE":
                    stateEnum = StateEnum.INACTIVE;
                    break;
                case "SUSPENDED":
                    stateEnum = StateEnum.SUSPENDED;
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            enterpriseUpdateManagerPort.updateEnterpriseStatus(id, stateEnum);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }

    
}
