package com.enterprises_management.enterprise.infraestructure.adapters.input.rest.data.response;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.models.EnterpriseType;
import com.enterprises_management.enterprise.domain.models.Location;
import com.enterprises_management.enterprise.domain.models.PersonType;
import com.enterprises_management.enterprise.domain.models.TaxLiability;
import com.enterprises_management.enterprise.domain.models.TaxPayerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnterpriseCreateResponse {
    
    private UUID id;
    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch; 
    private String email;
    private String logo;

    List<TaxLiability> taxLiabilities;

    TaxPayerType taxPayerType;

    EnterpriseType enterpriseType; 

    PersonType personType;

    Location location;  

}
