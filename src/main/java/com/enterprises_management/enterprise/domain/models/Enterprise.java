package com.enterprises_management.enterprise.domain.models;

import java.util.List;
import java.util.UUID;

import com.enterprises_management.enterprise.domain.enums.StateEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enterprise {

    private UUID id;

    private String idUser;

    private String name;
    private String nit;
    private String DV;
    private String phone;
    private String branch; 
    private String email;
    private String logo;

    private StateEnum state;

    List<TaxLiability> taxLiabilities;

    TaxPayerType taxPayerType;

    EnterpriseType enterpriseType; 

    PersonType personType;

    Location location;
}
