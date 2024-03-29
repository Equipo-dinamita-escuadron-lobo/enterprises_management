package com.enterprises_management.enterprise.domain.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enterprise {

    private Long id;
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
